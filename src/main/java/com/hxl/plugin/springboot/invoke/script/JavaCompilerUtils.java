package com.hxl.plugin.springboot.invoke.script;

import com.hxl.plugin.springboot.invoke.Constant;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JavaCompilerUtils {
    public static JavaCompiler getSystemJavaCompiler() {
        try {
            JavaCompiler systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
            if (systemJavaCompiler != null) return systemJavaCompiler;
            File file = Constant.CONFIG_JAVAC_PATH.toFile();
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            Class<?> javaCompilerClass = urlClassLoader.loadClass("javax.tools.JavaCompiler");
            Class<?> javacToolClass = Class.forName("com.sun.tools.javac.api.JavacTool", true, urlClassLoader);
            Class<?> subclass = javacToolClass.asSubclass(javaCompilerClass);
            return (JavaCompiler) subclass.getConstructor().newInstance();
        } catch (Exception ignored) {
        }
        return null;
    }
}
