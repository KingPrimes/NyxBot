package com.nyx.bot.utils.http;


import com.nyx.bot.core.SpringValues;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ProxyUtils {
    /**
     * 获取可用的代理
     * @return Proxy 实例
     */
    public static Proxy getEffectiveProxyForUrl() {
        // 1. 尝试从 JVM 参数获取
        Proxy proxy = fromJvmArgs();
        if (proxy != null) return proxy;

        // 2. 尝试从环境变量获取 HTTP_PROXY / ALL_PROXY
        proxy = fromEnvVariables();
        if (proxy != null) return proxy;

        // 3. 尝试读取系统代理（Windows / Linux / macOS）
        proxy = fromSystemProxy();
        if (proxy != null) return proxy;

        // 4. 回退到 Spring 配置文件
        proxy = fromSpringConfig();
        if (proxy != null) return proxy;

        // 默认无代理
        return Proxy.NO_PROXY;
    }

    public static ProxySelector getProxySelector() {
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                Proxy effectiveProxy = ProxyUtils.getEffectiveProxyForUrl();
                if (uri == null) return List.of(Proxy.NO_PROXY);

                String host = uri.getHost();
                if (ProxyUtils.shouldBypassProxy(host)) return List.of(Proxy.NO_PROXY);

                return List.of(effectiveProxy != null ? effectiveProxy : Proxy.NO_PROXY);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress address, IOException ex) {
                log.error("Proxy connection failed: {}", uri, ex);
            }
        };
    }

    private static Proxy parseStandardProxy(String proxyUrl) {
        if (proxyUrl == null || proxyUrl.isEmpty()) return null;

        try {
            URI uri = new URI(proxyUrl);
            String scheme = uri.getScheme().toLowerCase();
            String host = uri.getHost();
            int port = uri.getPort();

            if (host == null || port == -1) return null;

            Proxy.Type type;
            if ("http".equals(scheme) || "https".equals(scheme)) {
                type = Proxy.Type.HTTP;
            } else if ("socks".equals(scheme) || "socks5".equals(scheme)) {
                type = Proxy.Type.SOCKS;
            } else {
                return null;
            }

            return new Proxy(type, new InetSocketAddress(host, port));
        } catch (Exception e) {
            log.warn("解析代理失败: {}", proxyUrl, e);
            return null;
        }
    }


    // region 来源 1：JVM 参数
    private static Proxy fromJvmArgs() {
        String proxyUrl = System.getProperty("http.proxy");
        if (proxyUrl == null || proxyUrl.isEmpty()) {
            proxyUrl = System.getProperty("socks.proxy");
        }
        return parseStandardProxy(proxyUrl);
    }
    // endregion

    // region 来源 2：环境变量
    private static Proxy fromEnvVariables() {
        String raw = System.getenv("HTTP_PROXY");
        if (raw == null || raw.isEmpty()) {
            raw = System.getenv("ALL_PROXY");
        }
        return parseStandardProxy(raw);
    }
    // endregion

    // region 来源 3：系统代理

    private static Proxy fromSystemProxy() {
        if (isWindows()) {
            return getWindowsSystemProxy();
        } else if (isLinux()) {
            return getLinuxSystemProxy();
        } else if (isMacOS()) {
            return getMacOSSystemProxy();
        }
        return null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    // endregion

    // region Windows 系统代理

    private static Proxy getWindowsSystemProxy() {
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String proxyServer = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains("ProxyServer")) {
                    String[] parts = line.trim().split("\\s+");
                    proxyServer = parts.length > 2 ? parts[2] : null;
                }
            }

            if (proxyServer != null && !proxyServer.isEmpty()) {
                String[] hp = proxyServer.split(":");
                if (hp.length >= 2) {
                    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hp[0], Integer.parseInt(hp[1])));
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // endregion

    // region Linux 系统代理（GNOME/GSettings）

    private static Proxy getLinuxSystemProxy() {
        try {
            ProcessBuilder pb = new ProcessBuilder("gsettings", "get", "org.gnome.system.proxy", "mode");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String mode = reader.readLine();

            if (mode != null && mode.contains("manual")) {
                pb = new ProcessBuilder("gsettings", "get", "org.gnome.system.proxy.http", "host");
                process = pb.start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String host = reader.readLine().replaceAll("'", "").trim();

                pb = new ProcessBuilder("gsettings", "get", "org.gnome.system.proxy.http", "port");
                process = pb.start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String portStr = reader.readLine().trim();

                int port = Integer.parseInt(portStr);

                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // endregion

    // region macOS 系统代理

    private static Proxy getMacOSSystemProxy() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/sbin/networksetup", "-getwebproxy", "Wi-Fi");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String host = null;
            int port = -1;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Server")) {
                    host = line.split(":")[1].trim();
                } else if (line.contains("Port")) {
                    port = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (host != null && port > 0) {
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    // endregion

    private static Proxy fromSpringConfig() {
        try {
            var utils = SpringUtils.getBean(SpringValues.class);
            String proxyUrl = utils.url;
            if (proxyUrl != null && !proxyUrl.isEmpty()) {
                return parseStandardProxy(proxyUrl);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }


    public static boolean shouldBypassProxy(String host) {
        if (host == null) return true;
        // 添加对本地地址（如 localhost、127.0.0.1）的判断
        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            return true;
        }
        Set<String> noProxyHosts = getNoProxyHosts();
        for (String pattern : noProxyHosts) {
            if (pattern.startsWith(".") && host.endsWith(pattern)) {
                return true;
            } else if (host.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> getNoProxyHosts() {
        Set<String> hosts = new HashSet<>();
        String noProxy = System.getenv("NO_PROXY");
        if (noProxy != null && !noProxy.isEmpty()) {
            for (String h : noProxy.split(",")) {
                hosts.add(h.trim());
            }
        }
        return hosts;
    }
}
