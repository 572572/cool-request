package com.hxl.plugin.springboot.invoke.plugin.apifox;

import com.hxl.plugin.springboot.invoke.export.ApiExport;
import com.hxl.plugin.springboot.invoke.net.OkHttpRequest;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * api??
 */
public class ApiFoxExport extends OkHttpRequest implements ApiExport {
    private final ApifoxAPI apifoxAPI =new ApifoxAPI();
    @Override
    public boolean canExport() {
        return true;
    }

    @Override
    public void showCondition() {

    }

    @Override
    public OkHttpClient init(OkHttpClient.Builder builder) {
        return null;
    }

    @Override
    public boolean export(String json) {
        Map<String,Object> data =new HashMap<>();
        data.put("importFormat","openapi");
        data.put("apiOverwriteMode","methodAndPath");
        data.put("schemaOverwriteMode","merge");
        data.put("data",json);
        apifoxAPI.exportApi(2877496,data).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
        return false;
    }
}
