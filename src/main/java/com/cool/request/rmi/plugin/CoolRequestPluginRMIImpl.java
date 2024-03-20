package com.cool.request.rmi.plugin;

import com.cool.request.common.bean.RequestEnvironment;
import com.cool.request.common.constant.CoolRequestIdeaTopic;
import com.cool.request.common.state.CoolRequestEnvironmentPersistentComponent;
import com.cool.request.components.ComponentType;
import com.cool.request.components.http.DynamicController;
import com.cool.request.components.scheduled.DynamicSpringScheduled;
import com.cool.request.components.scheduled.DynamicXxlJobScheduled;
import com.cool.request.utils.StringUtils;
import com.cool.request.view.tool.UserProjectManager;
import com.intellij.openapi.application.ApplicationManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class CoolRequestPluginRMIImpl extends UnicastRemoteObject implements ICoolRequestPluginRMI {
    private UserProjectManager userProjectManager;

    public CoolRequestPluginRMIImpl(UserProjectManager userProjectManager) throws RemoteException {
        this.userProjectManager = userProjectManager;
    }

    @Override
    public void loadController(List<DynamicController> dynamicController) {
        userProjectManager.addComponent(ComponentType.CONTROLLER, dynamicController);
    }

    @Override
    public void loadScheduled(List<DynamicSpringScheduled> scheduleds) throws RemoteException {
        userProjectManager.addComponent(ComponentType.SCHEDULE, scheduleds);
    }

    @Override
    public void loadXXLScheduled(List<DynamicXxlJobScheduled> xxlJobScheduleds) throws RemoteException {
        userProjectManager.addComponent(ComponentType.SCHEDULE, xxlJobScheduleds);
    }

    @Override
    public void projectStartup(int availableTcpPort, int serverPort) throws RemoteException {
        userProjectManager.addSpringBootApplicationInstance(serverPort, availableTcpPort);
    }

    @Override
    public void loadGateway(String contextPath, int port, String prefix, String routeId) throws RemoteException {
        CoolRequestEnvironmentPersistentComponent.State instance = CoolRequestEnvironmentPersistentComponent.getInstance(userProjectManager.getProject());

        RequestEnvironment requestEnvironment = new RequestEnvironment();
        requestEnvironment.setId(StringUtils.calculateMD5(routeId));
        requestEnvironment.setEnvironmentName(routeId);
        requestEnvironment.setHostAddress("http://localhost:" + port + StringUtils.joinUrlPath(contextPath, addPrefix(prefix)));
        if (instance.getEnvironments().contains(requestEnvironment)) return;
        instance.getEnvironments().add(requestEnvironment);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(CoolRequestIdeaTopic.ENVIRONMENT_ADDED).event();
    }

    private String addPrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) return "";
        if (prefix.startsWith("/")) return prefix;
        return "/" + prefix;
    }

}
