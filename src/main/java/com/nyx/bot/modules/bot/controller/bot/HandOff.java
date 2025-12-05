package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.StringUtils;
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
            handoff(config);
            return config;
        }
    }

    /**
     * 解析环境变量，优先级：命令行 > 系统环境变量 > 默认配置
     * -debug 开启debug模式
     * -serverPort=8080 设置端口号
     * -wsServerEnable=true 设置为服务端
     * -wsServerUrl=/ws/shiro 设置服务端地址
     * -wsClientEnable=false 设置为客户端
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
        configureWsSettings(args, config, map);

        // 配置代理
        configureProxy(args, config, map);

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
    private static Integer resolveServerPort(String[] args, ConfigurableEnvironment env) {
        int defaultPort = env.getProperty("serverPort", Integer.class, 8080);
        int port = defaultPort;

        for (String arg : args) {
            String lower = arg.trim();
            if (lower.toLowerCase().startsWith("-serverport=")) {
                try {
                    Integer number = MatcherUtils.getNumber(arg);
                    if (number > 0 && number < 65535) {
                        port = number;
                    } else {
                        log.warn("Invalid serverPort:{}", number);
                    }
                } catch (Exception e) {
                    log.error("Invalid serverPort:{}", arg);
                }
                break;
            }
        }
        return port;
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
    private static void configureWsSettings(String[] args, NyxConfig config, Map<String, Object> map) {
        // 配置WebSocket服务端启用状态
        withArg(args, "-wsserverenable=", arg ->
                config.setIsServerOrClient(arg.toLowerCase().contains("true"))
        );
        map.put("shiro.ws.server.enable", config.getIsServerOrClient());

        // 配置WebSocket服务端URL
        withArg(args, "-wsserverurl=", arg ->
                config.setWsServerUrl(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("shiro.ws.server.url", config.getWsServerUrl());

        // 配置WebSocket客户端启用状态
        withArg(args, "-wsclientenable", arg ->
                config.setIsServerOrClient(!arg.toLowerCase().contains("true"))
        );
        map.put("shiro.ws.client.enable", !config.getIsServerOrClient());

        // 配置WebSocket客户端URL
        withArg(args, "-wsclienturl=", arg ->
                config.setWsClientUrl(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("shiro.ws.client.url", config.getWsClientUrl());

        // 配置Shiro令牌
        withArg(args, "-shirotoken=", arg ->
                config.setToken(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("shiro.token", config.getToken());
    }

    /**
     * 配置代理相关设置
     *
     * @param args   命令行参数数组
     * @param config Nyx配置对象
     * @param map    属性映射表，用于存储配置键值对
     */
    private static void configureProxy(String[] args, NyxConfig config, Map<String, Object> map) {
        // 配置HTTP代理地址
        withArg(args, "-httpproxy=", arg ->
                config.setHttpProxy(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("http.proxy", config.getHttpProxy());

        // 配置SOCKS代理地址
        withArg(args, "-socksproxy=", arg ->
                config.setSocksProxy(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("socks.proxy", config.getSocksProxy());

        // 配置代理用户名
        withArg(args, "-proxyuser=", arg ->
                config.setProxyUser(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("proxy.user", config.getProxyUser());

        // 配置代理密码
        withArg(args, "-proxypassword=", arg ->
                config.setProxyPassword(StringUtils.getSubString(arg, "=", ""))
        );
        map.put("proxy.password", config.getProxyPassword());
    }

    /**
     * 在命令行参数中查找指定前缀的参数并处理
     *
     * @param args     命令行参数数组
     * @param prefix   要查找的参数前缀
     * @param consumer 找到匹配参数时的处理函数
     */
    private static void withArg(String[] args, String prefix, Consumer<String> consumer) {
        // 查找第一个匹配指定前缀的参数并应用处理函数
        Arrays.stream(args)
                .map(a -> a.trim())
                .filter(a -> a.toLowerCase().startsWith(prefix.toLowerCase()))
                .findFirst()
                .ifPresent(consumer);
    }

    static class MapPropertySource extends org.springframework.core.env.MapPropertySource {
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
