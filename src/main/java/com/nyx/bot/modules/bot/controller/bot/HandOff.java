package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.utils.MatcherUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("all")
@Slf4j
public class HandOff {

    static File file = new File("./data/locate.yaml");

    public static Boolean handoff(NyxConfig config) {
        BufferedWriter writer;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Yaml yaml = new Yaml();
            Map<String, Object> load = new HashMap<>();
            Map<String, Object> o = yaml.load(new FileInputStream(file));
            if (o != null) {
                load = o;
            }
            load.put("serverPort", config.getServerPort() == null ? load.get("serverPort") : config.getServerPort());
            load.put("isServerOrClient", config.getIsServerOrClient() == null ? load.get("isServerOrClient") : config.getIsServerOrClient());
            load.put("wsServerUrl", config.getWsServerUrl() == null ? load.get("wsServerUrl") : config.getWsServerUrl());
            load.put("wsClientUrl", config.getWsClientUrl() == null ? load.get("wsClientUrl") : config.getWsClientUrl());
            load.put("token", config.getToken() == null ? load.get("token") : config.getToken());
            load.put("httpProxy", config.getHttpProxy() == null ? load.get("httpProxy") : config.getHttpProxy());
            load.put("socksProxy", config.getSocksProxy() == null ? load.get("socksProxy") : config.getSocksProxy());
            load.put("proxyUser", config.getProxyUser() == null ? load.get("proxyUser") : config.getProxyUser());
            load.put("proxyPassword", config.getProxyPassword() == null ? load.get("proxyPassword") : config.getProxyPassword());
            load.put("pluginPrefix", config.getPluginPrefix() == null ? load.get("pluginPrefix") : config.getPluginPrefix());
            String s = yaml.dumpAs(load, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(s);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static NyxConfig getConfig() {
        NyxConfig config = new NyxConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> load;
        try {
            load = yaml.load(new FileInputStream(file));
            config.setServerPort((Integer) load.get("serverPort"));
            config.setIsServerOrClient((Boolean) load.get("isServerOrClient"));
            config.setWsServerUrl((String) load.get("wsServerUrl"));
            config.setWsClientUrl((String) load.get("wsClientUrl"));
            config.setToken((String) load.get("token"));
            config.setHttpProxy((String) load.get("httpProxy"));
            config.setSocksProxy((String) load.get("socksProxy"));
            config.setProxyUser((String) load.get("proxyUser"));
            config.setProxyPassword((String) load.get("proxyPassword"));
            config.setPluginPrefix((Boolean) load.get("pluginPrefix"));
            return config;
        } catch (Exception e) {
            config.setWsServerUrl("/ws/shiro");
            config.setWsClientUrl("ws://localhost:3001");
            config.setToken("");
            config.setServerPort(8080);
            config.setIsServerOrClient(true);
            config.setHttpProxy("");
            config.setSocksProxy("");
            config.setProxyUser("");
            config.setProxyPassword("");
            config.setPluginPrefix(false);
            handoff(config);
            return config;
        }
    }

    /**
     * 解析环境变量，优先级：命令行 > 系统环境变量 > 默认配置
     * -debug 开启debug模式
     * -serverPort=8080 设置端口号
     * -wsServerEnable 设置为服务端
     * -wsServerUrl=/ws/shiro 设置服务端地址
     * -wsClientEnable 设置为客户端
     * -wsClientUrl=ws://localhost:3001 设置客户端地址
     * -shiroToken=token 设置OneBot协议链接的Token
     * -httpProxy=http://127.0.0.1:7890  Http代理地址:端口
     * -socksProxy=socks5://127.0.0.1:7890  Sock5代理地址:端口
     * -proxyUser=user 代理认证用户
     * -proxyPassword=password 代理认证密码
     *
     * @param args 命令行参数
     * @return 环境变量配置
     */
    public static ConfigurableEnvironment getEnv(String[] args) {
        ConfigurableEnvironment env = new StandardEnvironment();
        NyxConfig config = getConfig();
        Map<String, Object> map = new HashMap<>();
        // 配置debug模式
        configureDebug(args, env, map);

        // 设置新的端口号
        Integer serverPort = resolveServerPort(args, env);
        if (!serverPort.equals(8080)) {
            config.setServerPort(serverPort);
        }
        map.put("server.port", config.getServerPort());

        // 配置ws OneBot设置
        configureWsSettings(args, env, config, map);

        // 配置代理
        configureProxy(args, env, config, map);

        // 配置插件前缀（是否需要艾特触发）
        resolvePluginPrefix(args, env, config);
        map.put("nyx.plugin-prefix", config.getPluginPrefix());

        MapPropertySource propertySource = new MapPropertySource("dynamicPort", map);
        env.getPropertySources().addFirst(propertySource);
        handoff(config);
        return env;
    }

    /**
     * 解析命令行参数中的服务器端口配置
     *
     * @param args 命令行参数数组
     * @param env  环境配置对象，用于获取默认端口
     * @return 解析出的端口号，如果未指定或无效则返回默认端口
     */
    /**
     * 解析端口号，优先级：CLI 参数 > 环境变量 SERVER_PORT > 默认 8080
     */
    private static Integer resolveServerPort(String[] args, ConfigurableEnvironment env) {
        int port = 8080;

        for (String arg : args) {
            String lower = arg.trim();
            if (lower.toLowerCase().startsWith("-serverport=")) {
                try {
                    Integer number = MatcherUtils.getNumber(arg);
                    if (number > 0 && number < 65535) {
                        port = number;
                    }
                } catch (Exception e) {
                    log.error("Invalid serverPort:{}", arg);
                }
                return port;
            }
        }
        // CLI 未指定时检查环境变量 SERVER_PORT
        return env.getProperty("SERVER_PORT", Integer.class, port);
    }

    /**
     * 配置调试模式
     *
     * @param args 命令行参数数组
     * @param env  环境配置对象
     * @param map  属性映射表，用于存储配置键值对
     */
    private static void configureDebug(String[] args, ConfigurableEnvironment env, Map<String, Object> map) {
        // 从环境变量中获取DEBUG配置
        String debugEnv = env.getProperty("DEBUG");
        if (debugEnv == null || debugEnv.isEmpty()) {
            debugEnv = env.getProperty("debug");
        }
        // 设置debug模式
        boolean isDebug = "true".equalsIgnoreCase(debugEnv) || Arrays.stream(args).anyMatch(arg -> arg.trim().equalsIgnoreCase("-debug"));
        if (isDebug) {
            map.put("logging.level.com.nyx.bot", "DEBUG");
        }
    }

    /**
     * 配置WebSocket相关设置
     *
     * @param args   命令行参数数组
     * @param config Nyx配置对象
     * @param map    属性映射表，用于存储配置键值对
     */
    /**
     * 配置 WebSocket / OneBot 设置，优先级：CLI > 环境变量 > locate.yaml
     */
    private static void configureWsSettings(String[] args, ConfigurableEnvironment env,
                                            NyxConfig config, Map<String, Object> map) {
        // WebSocket 服务端启用：CLI -wsserverenable 或环境变量 SHIRO_WS_SERVER_ENABLE
        boolean wsserverenable = Arrays.stream(args).anyMatch(arg -> arg.trim().equalsIgnoreCase("-wsserverenable"))
                || "true".equalsIgnoreCase(env.getProperty("SHIRO_WS_SERVER_ENABLE"));
        config.setIsServerOrClient(wsserverenable);

        // WebSocket 服务端 URL：CLI -wsServerUrl= 或环境变量 SHIRO_WS_SERVER_URL
        withArgOrEnv(args, env, "-wsserverurl=", "SHIRO_WS_SERVER_URL",
                v -> config.setWsServerUrl(v));
        map.put("shiro.ws.server.url", config.getWsServerUrl());

        // WebSocket 客户端状态
        if (wsserverenable) {
            map.put("shiro.ws.client.enable", false);
        } else {
            boolean wsclientenable = Arrays.stream(args).anyMatch(arg -> arg.trim().equalsIgnoreCase("-wsclientenable"))
                    || "true".equalsIgnoreCase(env.getProperty("SHIRO_WS_CLIENT_ENABLE"));
            config.setIsServerOrClient(!wsclientenable);
            map.put("shiro.ws.client.enable", !config.getIsServerOrClient());
        }
        map.put("shiro.ws.server.enable", config.getIsServerOrClient());

        // WebSocket 客户端 URL：CLI -wsClientUrl= 或环境变量 SHIRO_WS_CLIENT_URL
        withArgOrEnv(args, env, "-wsclienturl=", "SHIRO_WS_CLIENT_URL",
                v -> config.setWsClientUrl(v));
        map.put("shiro.ws.client.url", config.getWsClientUrl());

        // Shiro Token：CLI -shiroToken= 或环境变量 SHIRO_TOKEN
        withArgOrEnv(args, env, "-shirotoken=", "SHIRO_TOKEN",
                v -> config.setToken(v));
        map.put("shiro.token", config.getToken());
    }

    /**
     * 配置代理相关设置
     *
     * @param args   命令行参数数组
     * @param config Nyx配置对象
     * @param map    属性映射表，用于存储配置键值对
     */
    /**
     * 配置代理设置，优先级：CLI > 环境变量 > locate.yaml
     */
    private static void configureProxy(String[] args, ConfigurableEnvironment env,
                                       NyxConfig config, Map<String, Object> map) {
        withArgOrEnv(args, env, "-httpproxy=", "HTTP_PROXY",
                v -> config.setHttpProxy(v));
        map.put("http.proxy", config.getHttpProxy());

        withArgOrEnv(args, env, "-socksproxy=", "SOCKS_PROXY",
                v -> config.setSocksProxy(v));
        map.put("socks.proxy", config.getSocksProxy());

        withArgOrEnv(args, env, "-proxyuser=", "PROXY_USER",
                v -> config.setProxyUser(v));
        map.put("proxy.user", config.getProxyUser());

        withArgOrEnv(args, env, "-proxypassword=", "PROXY_PASSWORD",
                v -> config.setProxyPassword(v));
        map.put("proxy.password", config.getProxyPassword());
    }

    /**
     * 配置插件前缀，优先级：CLI > 环境变量 PLUGIN_PREFIX > locate.yaml
     */
    private static void resolvePluginPrefix(String[] args, ConfigurableEnvironment env, NyxConfig config) {
        if (Arrays.stream(args).anyMatch(arg -> arg.trim().equalsIgnoreCase("-pluginprefix"))) {
            config.setPluginPrefix(true);
            return;
        }
        String envVal = env.getProperty("PLUGIN_PREFIX");
        if ("true".equalsIgnoreCase(envVal)) {
            config.setPluginPrefix(true);
        }
    }

    /**
     * 在命令行参数中查找指定前缀的参数并处理
     *
     * @param args     命令行参数数组
     * @param prefix   要查找的参数前缀
     * @param consumer 找到匹配参数时的处理函数
     */
    /**
     * 从 CLI 参数中查找指定前缀的值，应用于 consumer。
     * CLI 未找到时回退到环境变量（Spring Environment 会同时读取系统属性和环境变量）。
     */
    private static void withArgOrEnv(String[] args, ConfigurableEnvironment env,
                                     String prefix, String envKey, Consumer<String> consumer) {
        // 先查 CLI 参数
        String cliVal = Arrays.stream(args)
                .map(a -> a.trim().toLowerCase())
                .filter(a -> a.startsWith(prefix.toLowerCase()))
                .findFirst()
                .map(a -> {
                    int eq = a.indexOf('=');
                    return eq >= 0 ? a.substring(eq + 1) : "";
                })
                .orElse(null);
        if (cliVal != null && !cliVal.isEmpty()) {
            consumer.accept(cliVal);
            return;
        }
        // CLI 未指定时回退到环境变量
        String envVal = env.getProperty(envKey);
        if (envVal != null && !envVal.isEmpty()) {
            consumer.accept(envVal);
        }
    }

    static class MapPropertySource extends org.springframework.core.env.MapPropertySource {
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
