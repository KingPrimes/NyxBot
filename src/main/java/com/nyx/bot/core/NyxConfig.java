package com.nyx.bot.core;

import lombok.Data;


@Data
public class NyxConfig {

    // server 端口
    Integer serverPort = 8080;

    // bot blacklist or whitelist
    // true for whitelist, false for blacklist
    Boolean isBlackOrWhite = true;

    // websocket server or client
    // true for server, false for client
    Boolean isServerOrClient = true;

    // websocket client url
    String wsClientUrl = "ws://localhost:3001";

    // websocket server url
    String wsServerUrl = "/ws/shiro";
}
