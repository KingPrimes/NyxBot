package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.NyxConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
            return config;
        } catch (Exception e) {
            config.setWsServerUrl("/ws/shiro");
            config.setWsClientUrl("ws://localhost:3001");
            config.setToken("");
            config.setServerPort(8080);
            config.setIsServerOrClient(true);
            handoff(config);
            return config;
        }
    }

    public static ConfigurableEnvironment getEnv() {
        ConfigurableEnvironment env = new StandardEnvironment();
        NyxConfig config = getConfig();
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", config.getServerPort()); // 设置新的端口号
        map.put("shiro.ws.server.enable", config.getIsServerOrClient());
        map.put("shiro.ws.server.url", config.getWsServerUrl());
        map.put("shiro.ws.client.enable", !config.getIsServerOrClient());
        map.put("shiro.ws.client.url", config.getWsClientUrl());
        MapPropertySource propertySource = new MapPropertySource("dynamicPort", map);
        log.debug("Env AddFirst:{}", propertySource);
        env.getPropertySources().addFirst(propertySource);
        return env;
    }

    static class MapPropertySource extends org.springframework.core.env.MapPropertySource {
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
