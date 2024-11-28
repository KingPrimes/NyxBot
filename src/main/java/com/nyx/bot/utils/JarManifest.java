package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.jar.Manifest;

@Slf4j
public class JarManifest {
    static volatile HashMap<URI, Manifest> uriManifestMap;

    /**
     * 通过读取读取classpath文件的方式获取Manifest
     */
    public static Manifest getManifestFromClasspath(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI codeJarUri = null;
        try {
            codeJarUri = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        } catch (URISyntaxException e) {
           log.error(e.getMessage());
        }
        if (codeJarUri == null) {
            return null;
        }
        if (codeJarUri.getScheme().equals("jar")) {
            String newPath = codeJarUri.getSchemeSpecificPart();
            String suffix = "!/BOOT-INF/classes!/";
            if (newPath.endsWith(suffix)) {
                newPath = newPath.substring(0, newPath.length() - suffix.length());
            }
            if (newPath.endsWith("!/")) {
                newPath = newPath.substring(0, newPath.length() - 2);
            }
            try {
                codeJarUri = new URI(newPath);
            } catch (URISyntaxException e) {
                log.error(e.getMessage());
            }
        }

        if (uriManifestMap == null) {
            synchronized (JarManifest.class) {
                if (uriManifestMap == null) {
                    try {
                        uriManifestMap = readClasspathAllManifest();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return uriManifestMap.get(codeJarUri);
    }

    private static HashMap<URI, Manifest> readClasspathAllManifest() throws Exception {
        HashMap<URI, Manifest> manifestMap = new HashMap<>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources =
                resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/MANIFEST.MF");
        for (org.springframework.core.io.Resource resource : resources) {
            URL manifestUrl = resource.getURL();
            int lastIndex;
            String manifestPath;
            if (manifestUrl.getProtocol().equals("file")) {
                manifestPath = manifestUrl.toString();
                lastIndex = manifestPath.indexOf("META-INF/MANIFEST.MF");

            } else if (manifestUrl.getProtocol().equals("jar")) {
                manifestPath = manifestUrl.getPath();
                lastIndex = manifestPath.indexOf("!/META-INF/MANIFEST.MF");

            } else {
                log.error("jar位置的格式不支持");
                continue;
            }
            URI jarUri = new URI(manifestPath.substring(0, lastIndex));
            try (InputStream inputStream = resource.getInputStream()) {
                Manifest manifest = new Manifest(inputStream);
                manifestMap.put(jarUri, manifest);
            }
        }
        return manifestMap;
    }
}
