package com.nyx.bot.common.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.config.ConfigConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;


@Data
public class NyxConfig {

    // server 端口
    @Min(value = 1, message = "端口号不能小于1")
    @Max(value = 65535, message = "端口号不能大于65535")
    Integer serverPort = 8080;

    // websocket server or client
    // true for server, false for client
    Boolean isServerOrClient = true;

    // websocket client url
    @NotEmpty(message = "config.client.url.not.empty")
    String wsClientUrl = "ws://localhost:3001";

    // websocket server url
    @NotEmpty(message = "config.server.url.not.empty")
    String wsServerUrl = "/ws/shiro";

    String token;

    String httpProxy;

    String socksProxy;

    String proxyUser;

    String proxyPassword;

    // 插件前缀，是否使用艾特触发指令，默认为false
    Boolean pluginPrefix = false;

    /** 当前选中的绘图插件名称（持久化到 locate.yaml） */
    String pluginName;

    // 不进行序列化
    @JsonIgnore
    public boolean isValidateClientUrl() {
        return wsClientUrl.matches("^(ws|wss)://[\\w.-]+(:\\d+)?(/([\\w/_.-]*(\\?\\S+)?)?)?$");
    }

    @JsonIgnore
    public boolean isValidateServerUrl() {
        return wsServerUrl.matches("^/([A-z]+)/?([A-z]+)?");
    }

    // ======== YAML 序列化辅助方法 ========

    /**
     * 将全部字段转为 Map（用于完整写入 locate.yaml）。
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigConstants.SERVER_PORT, serverPort);
        map.put(ConfigConstants.IS_SERVER_OR_CLIENT, isServerOrClient);
        map.put(ConfigConstants.WS_SERVER_URL, wsServerUrl);
        map.put(ConfigConstants.WS_CLIENT_URL, wsClientUrl);
        map.put(ConfigConstants.TOKEN, token);
        map.put(ConfigConstants.HTTP_PROXY, httpProxy);
        map.put(ConfigConstants.SOCKS_PROXY, socksProxy);
        map.put(ConfigConstants.PROXY_USER, proxyUser);
        map.put(ConfigConstants.PROXY_PASSWORD, proxyPassword);
        map.put(ConfigConstants.PLUGIN_PREFIX, pluginPrefix);
        map.put(ConfigConstants.PLUGIN_NAME, pluginName);
        return map;
    }

    /**
     * 将非 null 字段合并到目标 Map（用于部分更新时保留已有值）。
     */
    public void mergeInto(Map<String, Object> target) {
        putIfNonNull(target, ConfigConstants.SERVER_PORT, serverPort);
        putIfNonNull(target, ConfigConstants.IS_SERVER_OR_CLIENT, isServerOrClient);
        putIfNonNull(target, ConfigConstants.WS_SERVER_URL, wsServerUrl);
        putIfNonNull(target, ConfigConstants.WS_CLIENT_URL, wsClientUrl);
        putIfNonNull(target, ConfigConstants.TOKEN, token);
        putIfNonNull(target, ConfigConstants.HTTP_PROXY, httpProxy);
        putIfNonNull(target, ConfigConstants.SOCKS_PROXY, socksProxy);
        putIfNonNull(target, ConfigConstants.PROXY_USER, proxyUser);
        putIfNonNull(target, ConfigConstants.PROXY_PASSWORD, proxyPassword);
        putIfNonNull(target, ConfigConstants.PLUGIN_PREFIX, pluginPrefix);
        putIfNonNull(target, ConfigConstants.PLUGIN_NAME, pluginName);
    }

    private static void putIfNonNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
