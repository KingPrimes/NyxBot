package com.nyx.bot.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


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

    // 不进行序列化
    @JsonIgnore
    public boolean isValidateClientUrl() {
        return wsClientUrl.matches("^(ws|wss)://[\\w.-]+(:\\d+)?(/([\\w/_.-]*(\\?\\S+)?)?)?$");
    }

    @JsonIgnore
    public boolean isValidateServerUrl() {
        return wsServerUrl.matches("^/([A-z]+)/?([A-z]+)?");
    }
}
