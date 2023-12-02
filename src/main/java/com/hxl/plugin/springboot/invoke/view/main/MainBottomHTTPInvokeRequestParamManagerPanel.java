package com.hxl.plugin.springboot.invoke.view.main;

import com.hxl.plugin.springboot.invoke.IdeaTopic;
import com.hxl.plugin.springboot.invoke.bean.BeanInvokeSetting;
import com.hxl.plugin.springboot.invoke.invoke.ControllerInvoke;
import com.hxl.plugin.springboot.invoke.model.RequestMappingModel;
import com.hxl.plugin.springboot.invoke.model.SpringMvcRequestMappingSpringInvokeEndpoint;
import com.hxl.plugin.springboot.invoke.springmvc.*;
import com.hxl.plugin.springboot.invoke.net.*;
import com.hxl.plugin.springboot.invoke.utils.*;
import com.hxl.plugin.springboot.invoke.view.IRequestParamManager;
import com.hxl.plugin.springboot.invoke.view.ReflexSettingUIPanel;
import com.hxl.plugin.springboot.invoke.view.page.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainBottomHTTPInvokeRequestParamManagerPanel extends JPanel
        implements IRequestParamManager, HTTPParamApply {
    private final Project project;
    private static final List<MapRequest> mapRequest = new ArrayList<>();
    private final JComboBox<HttpMethod> requestMethodComboBox = new ComboBox<>(HttpMethod.getValues());
    private final RequestHeaderPage requestHeaderPage = new RequestHeaderPage();
    private final JTextField requestUrlTextField = new JBTextField();
    private final JButton sendRequestButton = new JButton("Send");
    private final JPanel modelSelectPanel = new JPanel(new BorderLayout());
    private final ComboBox<String> httpInvokeModelComboBox = new ComboBox<>(new String[]{"http", "reflex"});
    private final UrlParamPage urlParamPage = new UrlParamPage();
    private JBTabs httpParamTab;
    private RequestBodyPage requestBodyPage;
    private TabInfo reflexInvokePanelTabInfo;
    private RequestMappingModel requestMappingModel;
    private final MainBottomHTTPInvokeViewPanel mainBottomHTTPInvokeViewPanel;
    private ScriptPage scriptPage;
    private RequestManager requestManager;

    public MainBottomHTTPInvokeRequestParamManagerPanel(Project project,
                                                        MainBottomHTTPInvokeViewPanel mainBottomHTTPInvokeViewPanel) {
        this.project = project;
        this.mainBottomHTTPInvokeViewPanel = mainBottomHTTPInvokeViewPanel;
        init();
        initEvent();
    }

    @Override
    public void applyParam(ControllerInvoke.ControllerRequestData controllerRequestData) {
        for (MapRequest request : mapRequest) {
            request.configRequest(controllerRequestData);
        }
        if (StringUtils.isEmpty(controllerRequestData.getContentType())) {
            controllerRequestData.setContentType(MediaTypes.TEXT);
        }
        controllerRequestData.setHeader("content-type", controllerRequestData.getContentType().toLowerCase(Locale.ROOT));
    }

    public HttpMethod getHttpMethod() {
        return HttpMethod.parse(requestMethodComboBox.getSelectedItem());
    }

    public String getRequestBody() {
        return requestBodyPage.getTextRequestBody();
    }

    /**
     * Load reflex panel if reflection is selected
     */
    private void loadReflexInvokePanel(boolean show) {
        if (show) {
            ReflexSettingUIPanel reflexSettingUIPanel = (ReflexSettingUIPanel) reflexInvokePanelTabInfo.getComponent();
            if (requestMappingModel != null) {
                reflexSettingUIPanel.setRequestMappingInvokeBean(requestMappingModel.getController());
            }
            httpParamTab.addTab(reflexInvokePanelTabInfo);
            return;
        }
        httpParamTab.removeTab(reflexInvokePanelTabInfo);
    }

    public ScriptPage getScriptPage() {
        return scriptPage;
    }

    public void setSendRequestClickEvent(ActionListener actionListener) {
        sendRequestButton.addActionListener(actionListener);
    }

    private void initEvent() {
        // 发送请求按钮监听

        httpInvokeModelComboBox.addItemListener(e -> {
            Object item = e.getItem();
            loadReflexInvokePanel(!"HTTP".equalsIgnoreCase(item.toString()));
        });

        //listener controller selected
        project.getMessageBus()
                .connect()
                .subscribe(IdeaTopic.CONTROLLER_CHOOSE_EVENT, (IdeaTopic.ControllerChooseEventListener) this::loadControllerInfo);
    }

    private void init() {
        setLayout(new BorderLayout(0, 0));
        final JPanel httpParamInputPanel = new JPanel();
        httpParamInputPanel.setLayout(new BorderLayout(0, 0));

        modelSelectPanel.add(httpInvokeModelComboBox, BorderLayout.WEST);
        modelSelectPanel.add(requestMethodComboBox, BorderLayout.CENTER);
        requestUrlTextField.setColumns(45);
        requestUrlTextField.setText(SocketUtils.getSocketUtils().getPort(project)+"");
        httpParamInputPanel.add(modelSelectPanel, BorderLayout.WEST);
        httpParamInputPanel.add(requestUrlTextField);
        httpParamInputPanel.add(sendRequestButton, BorderLayout.EAST);
        add(httpParamInputPanel, BorderLayout.NORTH);

        httpParamTab = new JBTabsImpl(project);

        //request header input page
        mapRequest.add(requestHeaderPage);
        TabInfo headTab = new TabInfo(requestHeaderPage);
        headTab.setText("Header");
        httpParamTab.addTab(headTab);

        //url param input page
        mapRequest.add(urlParamPage);
        TabInfo urlParamPageTabInfo = new TabInfo(urlParamPage);
        urlParamPageTabInfo.setText("Param");
        httpParamTab.addTab(urlParamPageTabInfo);

        //request body input page
        requestBodyPage = new RequestBodyPage(project);
        mapRequest.add(requestBodyPage);
        TabInfo requestBodyTabInfo = new TabInfo(requestBodyPage);
        requestBodyTabInfo.setText("Body");
        httpParamTab.addTab(requestBodyTabInfo);

        //script input page
        scriptPage = new ScriptPage(project);
        TabInfo tabInfo = new TabInfo(scriptPage);
        tabInfo.setText("Script");
        httpParamTab.addTab(tabInfo);

        add(httpParamTab.getComponent(), BorderLayout.CENTER);

        reflexInvokePanelTabInfo = new TabInfo(new ReflexSettingUIPanel());
        reflexInvokePanelTabInfo.setText("Invoke Setting");

    }

    /**
     * clear all request param
     */
    public void clearAllRequestParam() {
        requestBodyPage.setJsonBodyText("");
        requestBodyPage.setXmlBodyText("");
        requestBodyPage.setRawBodyText("");
        setUrl("");
        setFormData(null);
        setUrlencodedBody(null);
        setUrlParam(null);
        setHttpHeader(null);
        this.requestMappingModel = null;
    }

    public IRequestParamManager getRequestParamManager() {
        return this;
    }

    private void loadControllerInfo(RequestMappingModel requestMappingModel) {
        clearAllRequestParam();
        this.requestMappingModel = requestMappingModel;
        this.sendRequestButton.setEnabled(mainBottomHTTPInvokeViewPanel.canEnabledSendButton(requestMappingModel.getController().getId()));

        SpringMvcRequestMappingSpringInvokeEndpoint invokeBean = requestMappingModel.getController();
        String base = "http://localhost:" + requestMappingModel.getServerPort() + requestMappingModel.getContextPath();
        //从缓存中加载以前的设置
        RequestCache requestCache = RequestParamCacheManager.getCache(requestMappingModel.getController().getId());
        String url = getUrlString(requestMappingModel, requestCache, base);

        requestUrlTextField.setText(url);
        if (requestCache == null) requestCache = createDefaultRequestCache(requestMappingModel);

        getRequestParamManager().setInvokeHttpMethod(requestCache.getInvokeModelIndex());//调用方式
        getRequestParamManager().setHttpMethod(HttpMethod.parse(invokeBean.getHttpMethod().toUpperCase()));//http接口
        getRequestParamManager().setHttpHeader(requestCache.getHeaders());
        getRequestParamManager().setUrlParam(requestCache.getUrlParams());
        getRequestParamManager().setRequestBodyType(requestCache.getRequestBodyType());
        getRequestParamManager().setUrlencodedBody(requestCache.getUrlencodedBody());
        getRequestParamManager().setFormData(requestCache.getFormDataInfos());
        getRequestParamManager().setRequestBody(requestCache.getRequestBodyType(), requestCache.getRequestBody());
        scriptPage.setScriptText(requestCache.getRequestScript(), requestCache.getResponseScript());
        //是否显示反射设置面板
        Object selectedItem = httpInvokeModelComboBox.getSelectedItem();
        loadReflexInvokePanel(!"HTTP".equalsIgnoreCase(selectedItem == null ? "" : selectedItem.toString()));
    }

    @NotNull
    private String getUrlString(RequestMappingModel requestMappingModel, RequestCache requestCache, String base) {
        String url = requestCache != null ? requestCache.getUrl() : base + requestMappingModel.getController().getUrl();
        //如果有缓存，但是开头不是当前的主机、端口、和上下文,但是要保存请求参数
        if (requestCache != null && !url.startsWith(base)) {
            String query = "";
            try {
                query = new URL(url).getQuery();
            } catch (MalformedURLException ignored) {
            }
            if (query == null) query = "";
            url = base + requestMappingModel.getController().getUrl() + "?" + query;
        }
        return url;
    }

    private RequestCache createDefaultRequestCache(RequestMappingModel requestMappingModel) {
        HttpRequestInfo httpRequestInfo = SpringMvcRequestMappingUtils.getHttpRequestInfo(requestMappingModel);
        String json = "";
        if (httpRequestInfo.getRequestBody() instanceof JSONObjectBody) {
            json = ObjectMappingUtils.toJsonString(((JSONObjectBody) httpRequestInfo.getRequestBody()).getJson());
        }
        if (httpRequestInfo.getRequestBody() instanceof StringBody) {
            json = ObjectMappingUtils.toJsonString(((StringBody) httpRequestInfo.getRequestBody()).getValue());
        }
        return RequestCache.RequestCacheBuilder.aRequestCache()
                .withInvokeModelIndex(1)
                .withResponseScript("")
                .withRequestScript("")
                .withHeaders(httpRequestInfo.getHeaders().stream().map(requestParameterDescription ->
                        new KeyValue(requestParameterDescription.getName(), "")).collect(Collectors.toList()))
                .withUrlParams(httpRequestInfo.getUrlParams().stream().map(requestParameterDescription ->
                        new KeyValue(requestParameterDescription.getName(), "")).collect(Collectors.toList()))
                .withRequestBodyType(httpRequestInfo.getContentType())
                .withRequestBody(json)
                .withUrlencodedBody(httpRequestInfo.getUrlencodedBody().stream().map(requestParameterDescription ->
                        new KeyValue(requestParameterDescription.getName(), "")).collect(Collectors.toList()))
                .withFormDataInfos(httpRequestInfo.getFormDataInfos().stream().map(requestParameterDescription ->
                        new FormDataInfo(requestParameterDescription.getName(),
                                "", requestParameterDescription.getType())).collect(Collectors.toList()))
                .build();
    }

    @Override
    public int getInvokeModelIndex() {
        return httpInvokeModelComboBox.getSelectedIndex();
    }

    @Override
    public RequestMappingModel getCurrentRequestMappingModel() {
        return this.requestMappingModel;
    }

    @Override
    public BeanInvokeSetting getBeanInvokeSetting() {
        return ((ReflexSettingUIPanel) reflexInvokePanelTabInfo.getComponent()).getBeanInvokeSetting();
    }

    @Override
    public String getUrl() {
        return requestUrlTextField.getText();
    }

    @Override
    public int getInvokeHttpMethod() {
        return httpInvokeModelComboBox.getSelectedIndex();
    }

    @Override
    public List<KeyValue> getUrlencodedBody() {
        return requestBodyPage.getUrlencodedBody();
    }

    @Override
    public List<KeyValue> getHttpHeader() {
        return requestHeaderPage.getTableMap();
    }

    @Override
    public List<KeyValue> getUrlParam() {
        return urlParamPage.getTableMap();
    }

    @Override
    public List<FormDataInfo> getFormData() {
        return requestBodyPage.getFormDataInfo();
    }

    @Override
    public void setUrl(String url) {
        requestUrlTextField.setText(url);
    }

    @Override
    public void setHttpMethod(HttpMethod method) {
        requestMethodComboBox.setSelectedItem(method);
    }

    @Override
    public void setInvokeHttpMethod(int index) {
        httpInvokeModelComboBox.setSelectedIndex(index);
    }

    @Override
    public void setHttpHeader(List<KeyValue> value) {
        requestHeaderPage.setTableData(Optional.ofNullable(value).orElse(new ArrayList<>()));
    }

    @Override
    public void setUrlParam(List<KeyValue> value) {
        urlParamPage.setTableData(Optional.ofNullable(value).orElse(new ArrayList<>()));
    }

    @Override
    public void setFormData(List<FormDataInfo> value) {
        requestBodyPage.setFormData(Optional.ofNullable(value).orElse(new ArrayList<>()));
    }

    @Override
    public void setUrlencodedBody(List<KeyValue> value) {
        requestBodyPage.setUrlencodedBodyTableData(Optional.ofNullable(value).orElse(new ArrayList<>()));
    }

    @Override
    public void setRequestBody(String type, String body) {
        if (type == null || body == null) return;
        if (type.contains("json")) {
            requestBodyPage.setJsonBodyText(ObjectMappingUtils.format(body));
            return;
        }
        if (type.contains("xml")) {
            requestBodyPage.setXmlBodyText(body);
            return;
        }
        requestBodyPage.setRawBodyText(body);
    }

    @Override
    public String getRequestBodyType() {
        return requestBodyPage.getSelectedRequestBodyType();
    }

    @Override
    public void setRequestBodyType(String type) {
        requestBodyPage.setRequestBodyType(type);
    }

    @Override
    public void setSendButtonEnabled(boolean b) {
        this.sendRequestButton.setEnabled(b);
    }

    @Override
    public String getRequestScript() {
        return scriptPage.getRequestScriptText();
    }

    @Override
    public String getResponseScript() {
        return scriptPage.getResponseScriptText();
    }

}