package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Slf4j
public class JarManifest {

    /**
     * 通过读取读取classpath文件的方式获取Manifest
     */
    public static Manifest getManifestFromClasspath() {
        try {
            // 获取当前类的保护代码源（即JAR文件）
            var url = JarManifest.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            if (url.getScheme().equals("jar")) {
                String newPath = url.getSchemeSpecificPart();
                String suffix = "/!BOOT-INF/classes/!/";
                if (newPath.endsWith(suffix)) {
                    newPath = newPath.substring(0, newPath.length() - suffix.length());
                }
                if (newPath.endsWith("!/")) {
                    newPath = newPath.substring(0, newPath.length() - 2);
                }
                try {
                    url = new URI(newPath);
                } catch (URISyntaxException e) {
                    log.error(e.getMessage());
                }
            }
            String replace = url.toString().replace("nested:/", "");

            // 打开JAR文件
            JarFile jarFile = new JarFile(replace);

            // 获取JAR文件中的MANIFEST.MF条目
            JarEntry manifestEntry = jarFile.getJarEntry("META-INF/MANIFEST.MF");

            if (manifestEntry != null) {
                // 读取MANIFEST.MF文件
                InputStream inputStream = jarFile.getInputStream(manifestEntry);
                Manifest manifest = new Manifest(inputStream);
                inputStream.close();
                return manifest;
            } else {
                log.error("MANIFEST.MF 文件不存在");
            }

            // 关闭JAR文件
            jarFile.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
