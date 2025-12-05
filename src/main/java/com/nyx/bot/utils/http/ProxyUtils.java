package com.nyx.bot.utils.http;

import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.SpringValues;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

@Slf4j
public class ProxyUtils {
    /**
     * 获取可用的代理
     *
     * @return Proxy 实例
     */
    public static Proxy getEffectiveProxyForUrl() {
        // 1. 尝试从 JVM 参数获取
        Proxy proxy = fromJvmArgs();
        // log.debug("获取JVM代理：{}", proxy);
        if (!proxy.equals(Proxy.NO_PROXY))
            return proxy;

        // 2. 尝试从环境变量获取 HTTP_PROXY / ALL_PROXY
        proxy = fromEnvVariables();
        // log.debug("获取环境代理：{}", proxy);
        if (!proxy.equals(Proxy.NO_PROXY))
            return proxy;

        // 3. 尝试读取系统代理（Windows / Linux / macOS）
        proxy = fromSystemProxy();
        // log.debug("获取系统代理：{}", proxy);
        if (!proxy.equals(Proxy.NO_PROXY))
            return proxy;

        // 4. 回退到 Spring 配置文件
        proxy = fromSpringConfig();
        // log.debug("获取Spring代理：{}", proxy);
        if (!proxy.equals(Proxy.NO_PROXY))
            return proxy;

        // log.debug("无代理可用，返回默认无代理");
        // 默认无代理
        return Proxy.NO_PROXY;
    }

    private static Proxy parseStandardProxy(String proxyUrl, String username, String password) {
        if (proxyUrl == null || proxyUrl.isEmpty())
            return null;

        try {
            URI uri = new URI(proxyUrl);
            String scheme = uri.getScheme().toLowerCase();
            String host = uri.getHost();
            int port = uri.getPort();

            if (host == null || port == -1)
                return Proxy.NO_PROXY;

            Proxy.Type type;
            switch (scheme) {
                case "http", "https" -> type = Proxy.Type.HTTP;
                case "socks", "socks5" -> type = Proxy.Type.SOCKS;
                default -> {
                    return Proxy.NO_PROXY;
                }
            }

            // 解析用户名和密码
            setupProxyAuthentication(username, password, host, port, type);

            return new Proxy(type, new InetSocketAddress(host, port));
        } catch (Exception e) {
            log.warn("解析代理失败: {}", proxyUrl, e);
            return Proxy.NO_PROXY;
        }
    }

    // region 来源 1：JVM 参数
    private static Proxy fromJvmArgs() {
        NyxConfig config = HandOff.getConfig();
        String proxyUrl = config.getHttpProxy() == null || config.getHttpProxy().isEmpty() ? config.getSocksProxy() : config.getHttpProxy();
        String username = config.getProxyUser();
        String password = config.getProxyPassword();
        return parseStandardProxy(proxyUrl, username, password);
    }
    // endregion

    // region 来源 2：环境变量
    private static Proxy fromEnvVariables() {
        String raw = System.getenv("HTTP_PROXY");
        String username = System.getenv("PROXY_USER");
        String password = System.getenv("PROXY_PASSWORD");
        if (raw == null || raw.isEmpty()) {
            raw = System.getenv("ALL_PROXY");
        }
        return parseStandardProxy(raw, username, password);
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
        return Proxy.NO_PROXY;
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
            ProcessBuilder pb = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String proxyServer = null;
            boolean proxyEnabled = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("ProxyServer")) {
                    String[] parts = line.trim().split("\\s+");
                    proxyServer = parts.length > 2 ? parts[2] : null;
                } else if (line.contains("ProxyEnable")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 2) {
                        proxyEnabled = "0x1".equals(parts[2]) || "1".equals(parts[2]);
                    }
                }
            }

            // 只有当代理启用且存在服务器地址时才返回代理
            if (proxyEnabled && proxyServer != null && !proxyServer.isEmpty()) {
                String[] hp = proxyServer.split(":");
                if (hp.length >= 2) {
                    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hp[0], Integer.parseInt(hp[1])));
                }
            }
        } catch (Exception ignored) {
        }
        return Proxy.NO_PROXY; // 否则继续向下查找
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
            } else {
                // mode 不是 manual，说明未启用代理
                return Proxy.NO_PROXY;
            }
        } catch (Exception ignored) {
            return Proxy.NO_PROXY;
        }
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
            boolean enabled = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Server")) {
                    host = line.split(":")[1].trim();
                } else if (line.contains("Port")) {
                    port = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.contains("Enabled")) {
                    enabled = line.split(":")[1].trim().equalsIgnoreCase("Yes");
                }
            }

            if (enabled && host != null && port > 0) {
                return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            }
        } catch (Exception ignored) {
        }
        return Proxy.NO_PROXY;
    }

    // endregion

    /**
     * 设置代理认证
     *
     * @param username 用户名
     * @param password 密码
     * @param host     代理主机
     * @param port     代理端口
     * @param type     代理类型
     */
    private static void setupProxyAuthentication(String username, String password, String host, int port,
                                                 Proxy.Type type) {
        // 对于 HTTP 代理，使用 Authenticator
        if (Proxy.Type.HTTP == type) {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    if (getRequestingHost() != null && getRequestingHost().equals(host)
                            && getRequestingPort() == port) {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                    return null;
                }
            });
        }
        // SOCKS 代理通常由 Java 自动处理认证信息
    }

    private static Proxy fromSpringConfig() {
        try {
            var utils = SpringUtils.getBean(SpringValues.class);
            String proxyUrl = utils.url;
            String username = utils.username;
            String password = utils.password;
            if (username == null || username.isEmpty())
                username = null;
            if (password == null || password.isEmpty())
                password = null;
            if (proxyUrl != null && !proxyUrl.isEmpty()) {
                return parseStandardProxy(proxyUrl, username, password);
            }
            return Proxy.NO_PROXY;
        } catch (Exception ignored) {
            return Proxy.NO_PROXY;
        }
    }
}
