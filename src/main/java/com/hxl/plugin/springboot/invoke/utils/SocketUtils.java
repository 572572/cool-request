package com.hxl.plugin.springboot.invoke.utils;

import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class SocketUtils {
    private static final SocketUtils socketUtils = new SocketUtils();
    private static final Map<Project, Integer> portMap = new HashMap<>();
    private SocketUtils() {
    }
    public static SocketUtils getSocketUtils() {
        return socketUtils;
    }

    public int getPort(Project project) {
        if (portMap.containsKey(project)) return portMap.get(project);
        int port = generatorPort();
        while (portMap.containsValue(port) && !canUse(port)) {
            port++;
        }
        portMap.put(project, port);
        return port;

    }

    private boolean canUse(int port) {
        try (SocketChannel localhost = SocketChannel.open(new InetSocketAddress("localhost", port))) {
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

    private int generatorPort() {
        int port = 33333;
        boolean next = true;
        while (next) {
            //链接成功表示这个socket用不成
            if (canUse(port)) {
                port++;
                continue;
            }
            next = false;
        }
        return port;
    }
}