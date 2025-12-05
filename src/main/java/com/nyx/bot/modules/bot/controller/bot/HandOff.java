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
import java.util.concurrent.atomic.AtomicReference;

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
     * -server.port=8080 设置端口号
     * -shiro.ws.server.enable=true 设置为服务端
     * -shiro.ws.server.url=/ws/shiro 设置服务端地址
     * -shiro.ws.client.enable=false 设置为客户端
     * -shiro.ws.client.url=ws://localhost:3001 设置客户端地址
     * -shiro.token= 添加token
     * -http.proxy = http://127.0.0.1:7890
     * -socks.proxy = socks5://127.0.0.1:7890
     * -proxy.user = user
     * -proxy.password = password
     *
     * @param args 命令行参数
     * @return 环境变量配置
     */
    public static ConfigurableEnvironment getEnv(String[] args) {
        ConfigurableEnvironment env = new StandardEnvironment();
        NyxConfig config = getConfig();
        Map<String, Object> map = new HashMap<>();
        String debugEnv = env.getProperty("DEBUG");
        if (debugEnv == null || debugEnv.isEmpty()) {
            debugEnv = env.getProperty("debug");
        }
        // 设置debug模式
        boolean isDebug = "true".equalsIgnoreCase(debugEnv) || Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-debug"));
        if (isDebug) {
            map.put("logging.level.com.nyx.bot", "DEBUG");
        }

        // 设置新的端口号
        AtomicReference<Integer> serverPort = new AtomicReference<>(env.getProperty("serverPort", Integer.class, 8080));
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-serverport=")).findFirst().ifPresent(arg -> {
            try {
                Integer number = MatcherUtils.getNumber(arg);
                if (number > 0 && number < 65535) {
                    serverPort.set(number);
                } else {
                    log.warn("Invalid serverPort:{}", number);
                }
            } catch (Exception e) {
                log.error("Invalid serverPort:{}", arg);
            }
        });

        if (serverPort.get() != 8080) {
            config.setServerPort(serverPort.get());
        }
        map.put("server.port", config.getServerPort());

        // 设置是否是服务端
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-wsserverenable=")).findFirst().ifPresent(arg -> {
            config.setIsServerOrClient(arg.toLowerCase().contains("true"));
        });
        map.put("shiro.ws.server.enable", config.getIsServerOrClient());

        // 设置服务端地址
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-wsserverurl=")).findFirst().ifPresent(arg -> {
            config.setWsServerUrl(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("shiro.ws.server.url", config.getWsServerUrl());

        // 配置是否是客户端
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-wsclientenable")).findFirst().ifPresent(arg -> {
            config.setIsServerOrClient(!arg.toLowerCase().contains("true"));
        });
        map.put("shiro.ws.client.enable", !config.getIsServerOrClient());

        // 配置客户端地址
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-wsclienturl=")).findFirst().ifPresent(arg -> {
            config.setWsClientUrl(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("shiro.ws.client.url", config.getWsClientUrl());

        // 配置token
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-shirotoken=")).findFirst().ifPresent(arg -> {
            config.setToken(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("shiro.token", config.getToken());

        // 配置http代理
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-httpproxy=")).findFirst().ifPresent(arg -> {
            config.setHttpProxy(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("http.proxy", config.getHttpProxy());

        // 配置socks代理
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-socksproxy=")).findFirst().ifPresent(arg -> {
            config.setSocksProxy(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("socks.proxy", config.getSocksProxy());

        // 配置代理用户
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-proxyuser=")).findFirst().ifPresent(arg -> {
            config.setProxyUser(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("proxy.user", config.getProxyUser());

        // 配置代理密码
        Arrays.stream(args).filter(arg -> arg.toLowerCase().trim().startsWith("-proxypassword=")).findFirst().ifPresent(arg -> {
            config.setProxyPassword(StringUtils.getSubString(arg, "=", ""));
        });
        map.put("proxy.password", config.getProxyPassword());

        MapPropertySource propertySource = new MapPropertySource("dynamicPort", map);
        env.getPropertySources().addFirst(propertySource);
        handoff(config);
        return env;
    }

    static class MapPropertySource extends org.springframework.core.env.MapPropertySource {
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
