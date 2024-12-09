package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

@Slf4j
public class JarManifest {

    /**
     * 通过读取读取classpath文件的方式获取Manifest
     */
    public static Manifest manifestFromClasspath() {
        URL resourceUrl = JarManifest.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
        if (resourceUrl != null) {
            try (InputStream inputStream = resourceUrl.openStream()) {
                return new Manifest(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
