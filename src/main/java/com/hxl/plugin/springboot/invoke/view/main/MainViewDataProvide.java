package com.hxl.plugin.springboot.invoke.view.main;

import com.hxl.plugin.springboot.invoke.bean.RequestEnvironment;
import com.hxl.plugin.springboot.invoke.bean.RequestMappingWrapper;
import com.hxl.plugin.springboot.invoke.model.RequestMappingModel;
import org.jetbrains.annotations.NotNull;

public interface MainViewDataProvide {
    @NotNull
    public RequestEnvironment getSelectRequestEnvironment();

    public String applyUrl(RequestMappingWrapper requestMappingWrapper);

}