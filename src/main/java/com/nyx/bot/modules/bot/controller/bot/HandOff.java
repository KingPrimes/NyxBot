package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.config.ConfigConstants;
import com.nyx.bot.common.config.LocateYamlService;
import com.nyx.bot.common.core.NyxConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置入口门面。
 * <p>
 * 在 Spring Boot 启动前从 CLI 参数、环境变量和 {@code locate.yaml} 中读取配置，
 * 按优先级合并后持久化到 {@code locate.yaml}，并构造 Spring Environment。
 * YAML I/O 委托给 {@link LocateYamlService}。
 * </p>
 * <p>
 * 优先级：CLI 参数 > 环境变量 > 持久化值 > 默认值
 * </p>
 *
 * @author KingPrimes
 */
@Slf4j
public class HandOff {

    /**
     * 解析 CLI 参数和环境变量，构造 Spring Environment。
     *
     * @param args 命令行参数
     * @return 环境变量配置
     */
    public static ConfigurableEnvironment getEnv(String[] args) {
        var yamlService = new LocateYamlService();
        var persisted = yamlService.load();
        NyxConfig merged = resolveConfig(args, persisted);
        yamlService.save(toMap(merged));
        return buildEnvironment(merged, args);
    }

    /**
     * 从 {@code locate.yaml} 读取完整配置（运行时使用）。
     * <p>
     * 供 {@code ConfigLoadingController}、{@code ProxyUtils} 等运行时组件使用，
     * 不参与 Spring Boot 启动流程。
     * </p>
     */
    public static NyxConfig getConfig() {
        var yamlService = new LocateYamlService();
        Map<String, Object> persisted = yamlService.load();
        NyxConfig config = new NyxConfig();
        Env env = new Env(persisted);
        config.setServerPort(env.getInt(ConfigConstants.SERVER_PORT, 8080));
        config.setIsServerOrClient(env.getBool(ConfigConstants.IS_SERVER_OR_CLIENT, true));
        config.setWsServerUrl(env.getStr(ConfigConstants.WS_SERVER_URL, "/ws/shiro"));
        config.setWsClientUrl(env.getStr(ConfigConstants.WS_CLIENT_URL, "ws://localhost:3001"));
        config.setToken(env.getStr(ConfigConstants.TOKEN, ""));
        config.setHttpProxy(env.getStr(ConfigConstants.HTTP_PROXY, ""));
        config.setSocksProxy(env.getStr(ConfigConstants.SOCKS_PROXY, ""));
        config.setProxyUser(env.getStr(ConfigConstants.PROXY_USER, ""));
        config.setProxyPassword(env.getStr(ConfigConstants.PROXY_PASSWORD, ""));
        config.setPluginPrefix(env.getBool(ConfigConstants.PLUGIN_PREFIX, false));
        config.setPluginName(env.getStr(ConfigConstants.PLUGIN_NAME, ""));
        return config;
    }

    /**
     * 持久化配置到 {@code locate.yaml}（运行时使用）。
     * <p>
     * 仅更新非 null 字段，已有字段保持不变。
     * 供运行时 REST API 使用，不参与 Spring Boot 启动流程。
     * </p>
     */
    public static boolean handoff(NyxConfig config) {
        try {
            var yamlService = new LocateYamlService();
            Map<String, Object> data = yamlService.load();
            putIfNonNull(data, ConfigConstants.SERVER_PORT, config.getServerPort());
            putIfNonNull(data, ConfigConstants.IS_SERVER_OR_CLIENT, config.getIsServerOrClient());
            putIfNonNull(data, ConfigConstants.WS_SERVER_URL, config.getWsServerUrl());
            putIfNonNull(data, ConfigConstants.WS_CLIENT_URL, config.getWsClientUrl());
            putIfNonNull(data, ConfigConstants.TOKEN, config.getToken());
            putIfNonNull(data, ConfigConstants.HTTP_PROXY, config.getHttpProxy());
            putIfNonNull(data, ConfigConstants.SOCKS_PROXY, config.getSocksProxy());
            putIfNonNull(data, ConfigConstants.PROXY_USER, config.getProxyUser());
            putIfNonNull(data, ConfigConstants.PROXY_PASSWORD, config.getProxyPassword());
            putIfNonNull(data, ConfigConstants.PLUGIN_PREFIX, config.getPluginPrefix());
            putIfNonNull(data, ConfigConstants.PLUGIN_NAME, config.getPluginName());
            yamlService.save(data);
            return true;
        } catch (Exception e) {
            log.error("保存配置失败", e);
            return false;
        }
    }

    private static void putIfNonNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * 按优先级合并配置：CLI 参数 > 环境变量 > 持久化值 > 默认值
     */
    private static NyxConfig resolveConfig(String[] args, Map<String, Object> persisted) {
        NyxConfig config = new NyxConfig();
        Env env = new Env(persisted);

        config.setServerPort(resolveInt(args, "-serverport=", "SERVER_PORT",
                env.getInt(ConfigConstants.SERVER_PORT, 8080)));
        config.setIsServerOrClient(resolveBool(args, "-wsserverenable",
                "SHIRO_WS_SERVER_ENABLE",
                env.getBool(ConfigConstants.IS_SERVER_OR_CLIENT, true)));
        config.setWsServerUrl(resolveStr(args, "-wsserverurl=", "SHIRO_WS_SERVER_URL",
                env.getStr(ConfigConstants.WS_SERVER_URL, "/ws/shiro")));
        config.setWsClientUrl(resolveStr(args, "-wsclienturl=", "SHIRO_WS_CLIENT_URL",
                env.getStr(ConfigConstants.WS_CLIENT_URL, "ws://localhost:3001")));
        config.setToken(resolveStr(args, "-shirotoken=", "SHIRO_TOKEN",
                env.getStr(ConfigConstants.TOKEN, "")));
        config.setHttpProxy(resolveStr(args, "-httpproxy=", "HTTP_PROXY",
                env.getStr(ConfigConstants.HTTP_PROXY, "")));
        config.setSocksProxy(resolveStr(args, "-socksproxy=", "SOCKS_PROXY",
                env.getStr(ConfigConstants.SOCKS_PROXY, "")));
        config.setProxyUser(resolveStr(args, "-proxyuser=", "PROXY_USER",
                env.getStr(ConfigConstants.PROXY_USER, "")));
        config.setProxyPassword(resolveStr(args, "-proxypassword=", "PROXY_PASSWORD",
                env.getStr(ConfigConstants.PROXY_PASSWORD, "")));
        config.setPluginPrefix(resolveBool(args, "-pluginprefix", "PLUGIN_PREFIX",
                env.getBool(ConfigConstants.PLUGIN_PREFIX, false)));
        config.setPluginName(resolveStr(args, "-pluginname=", "PLUGIN_NAME",
                env.getStr(ConfigConstants.PLUGIN_NAME, "")));

        return config;
    }

    // ======== 优先级解析辅助方法 ========

    /**
     * 解析字符串配置，优先级：CLI 参数 > 环境变量 > fallback（持久化值 > 默认值）
     */
    private static String resolveStr(String[] args, String cliPrefix,
                                     String envKey, String fallback) {
        String fromCli = Arrays.stream(args)
                .map(String::trim)
                .filter(a -> a.regionMatches(true, 0, cliPrefix, 0,
                        cliPrefix.length()))
                .findFirst()
                .map(a -> {
                    int eq = a.indexOf('=');
                    return eq >= 0 ? a.substring(eq + 1) : "";
                })
                .orElse(null);
        if (fromCli != null) return fromCli;
        String fromEnv = System.getenv(envKey);
        return fromEnv != null && !fromEnv.isEmpty() ? fromEnv : fallback;
    }

    /**
     * 解析整型配置
     */
    private static Integer resolveInt(String[] args, String cliPrefix,
                                      String envKey, Integer fallback) {
        String str = resolveStr(args, cliPrefix, envKey,
                fallback != null ? fallback.toString() : null);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.warn("无效的整型配置 {}: {}, 使用默认值 {}", cliPrefix, str, fallback);
            return fallback;
        }
    }

    /**
     * 解析布尔配置（CLI 使用无值 flag）
     */
    private static Boolean resolveBool(String[] args, String cliFlag,
                                       String envKey, Boolean fallback) {
        if (Arrays.stream(args).anyMatch(a -> a.trim().equalsIgnoreCase(cliFlag))) {
            return true;
        }
        String fromEnv = System.getenv(envKey);
        if ("true".equalsIgnoreCase(fromEnv)) return true;
        if ("false".equalsIgnoreCase(fromEnv)) return false;
        return fallback;
    }

    /** 将 NyxConfig 转为可序列化的 Map */
    private static Map<String, Object> toMap(NyxConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigConstants.SERVER_PORT,         config.getServerPort());
        map.put(ConfigConstants.IS_SERVER_OR_CLIENT, config.getIsServerOrClient());
        map.put(ConfigConstants.WS_SERVER_URL,       config.getWsServerUrl());
        map.put(ConfigConstants.WS_CLIENT_URL,       config.getWsClientUrl());
        map.put(ConfigConstants.TOKEN,               config.getToken());
        map.put(ConfigConstants.HTTP_PROXY,          config.getHttpProxy());
        map.put(ConfigConstants.SOCKS_PROXY,         config.getSocksProxy());
        map.put(ConfigConstants.PROXY_USER,          config.getProxyUser());
        map.put(ConfigConstants.PROXY_PASSWORD,      config.getProxyPassword());
        map.put(ConfigConstants.PLUGIN_PREFIX,       config.getPluginPrefix());
        map.put(ConfigConstants.PLUGIN_NAME,         config.getPluginName());
        return map;
    }

    /**
     * 将配置转为 Spring Environment
     */
    private static ConfigurableEnvironment buildEnvironment(NyxConfig config, String[] args) {
        ConfigurableEnvironment env = new StandardEnvironment();
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", config.getServerPort());
        map.put("shiro.ws.server.enable", config.getIsServerOrClient());
        map.put("shiro.ws.server.url", config.getWsServerUrl());
        map.put("shiro.ws.client.enable", !config.getIsServerOrClient());
        map.put("shiro.ws.client.url", config.getWsClientUrl());
        map.put("shiro.token", config.getToken());
        map.put("http.proxy", config.getHttpProxy() != null ? config.getHttpProxy() : "");
        map.put("socks.proxy", config.getSocksProxy() != null ? config.getSocksProxy() : "");
        map.put("proxy.user", config.getProxyUser() != null ? config.getProxyUser() : "");
        map.put("proxy.password", config.getProxyPassword() != null ? config.getProxyPassword() : "");
        map.put("nyx.plugin-prefix", config.getPluginPrefix());
        if (isDebug(args, env)) {
            map.put("logging.level.com.nyx.bot", "DEBUG");
        }
        var ps = new MapPropertySource("dynamicPort", map);
        env.getPropertySources().addFirst(ps);
        return env;
    }

    /**
     * 检查是否启用调试模式（-debug 或 DEBUG=true）
     */
    private static boolean isDebug(String[] args, ConfigurableEnvironment env) {
        String debugEnv = env.getProperty("DEBUG");
        if (debugEnv == null || debugEnv.isEmpty()) {
            debugEnv = env.getProperty("debug");
        }
        return "true".equalsIgnoreCase(debugEnv)
                || Arrays.stream(args).anyMatch(a -> a.trim().equalsIgnoreCase("-debug"));
    }

    /**
     * 封装从持久化 Map 中读取配置，提供类型安全的方法。
     */
    static class Env {
        private final Map<String, Object> data;

        Env(Map<String, Object> data) { this.data = data; }

        String getStr(String key, String def) {
            Object v = data.get(key);
            return v instanceof String s ? s : def;
        }

        Integer getInt(String key, Integer def) {
            Object v = data.get(key);
            if (v instanceof Number n) return n.intValue();
            return def;
        }

        Boolean getBool(String key, Boolean def) {
            Object v = data.get(key);
            if (v instanceof Boolean b) return b;
            return def;
        }
    }
}
