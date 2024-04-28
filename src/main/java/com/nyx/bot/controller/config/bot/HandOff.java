package com.nyx.bot.controller.config.bot;

import com.nyx.bot.core.NyxConfig;
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
            load.put("serverPort", config.getServerPort() == null ? 8080 : config.getServerPort());
            load.put("isBlackOrWhite", config.getIsBlackOrWhite() == null || config.getIsBlackOrWhite());
            load.put("isStatusEnable", config.getIsStatusEnable() == null || config.getIsStatusEnable());
            load.put("isServerOrClient", config.getIsServerOrClient() == null || config.getIsServerOrClient());
            load.put("wsServerUrl", config.getWsServerUrl() == null ? "/ws/shiro" : config.getWsServerUrl());
            load.put("wsClientUrl", config.getWsClientUrl() == null ? "ws://localhost:3001" : config.getWsClientUrl());
            String s = yaml.dumpAs(load, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(s);
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Boolean isBW() {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> load = yaml.load(new FileInputStream(file));
            return (Boolean) load.get("isBlackOrWhite");
        } catch (Exception e) {
            handoff(new NyxConfig());
            return true;
        }
    }

    public static NyxConfig getConfig() {
        NyxConfig config = new NyxConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> load;
        try {
            load = yaml.load(new FileInputStream(file));
            config.setServerPort((Integer) load.get("serverPort"));
            config.setIsBlackOrWhite((Boolean) load.get("isBlackOrWhite"));
            config.setIsServerOrClient((Boolean) load.get("isServerOrClient"));
            config.setWsServerUrl((String) load.get("wsServerUrl"));
            config.setWsClientUrl((String) load.get("wsClientUrl"));
            config.setIsStatusEnable((Boolean) load.get("isStatusEnable"));
            return config;
        } catch (Exception e) {
            handoff(new NyxConfig());
            return config;
        }
    }

    public static ConfigurableEnvironment getEnv() {
        ConfigurableEnvironment env = new StandardEnvironment();
        NyxConfig config = getConfig();
        Map<String, Object> map = new HashMap<>();
        map.put("server.port", config.getServerPort() == null ? 8080 : config.getServerPort()); // 设置新的端口号
        map.put("shiro.ws.server.enable", config.getIsServerOrClient() == null || config.getIsServerOrClient());
        map.put("shiro.ws.server.url", config.getWsServerUrl() == null ? "/ws/shiro" : config.getWsServerUrl());
        map.put("shiro.ws.client.enable", config.getIsServerOrClient() != null && !config.getIsServerOrClient());
        map.put("shiro.ws.client.url", config.getWsClientUrl() == null ? "ws://localhost:3001" : config.getWsClientUrl());
        MapPropertySource propertySource = new MapPropertySource("dynamicPort", map);
        env.getPropertySources().addFirst(propertySource);
        return env;
    }

    static class MapPropertySource extends org.springframework.core.env.MapPropertySource {
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
    }


}
