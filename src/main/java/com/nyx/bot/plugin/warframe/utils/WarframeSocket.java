package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.AsyncUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class WarframeSocket extends WebSocketListener {

    private static final WarframeSocket socket = new WarframeSocket();
    WebSocket webSocket;

    public static WarframeSocket socket() {
        return socket;
    }

    public void connectServer(String url) {
        log.info("正在向{}链接...", url);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .pingInterval(45, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, this);
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "service close");
        }
    }


    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.info("链接已被关闭:{},{}", code, reason);
        super.onClosed(webSocket, code, reason);
        if (code == 1000) {
            return;
        }
        log.info("链接已被关闭将于5秒后重新链接");
        try {
            Thread.sleep(5000);
            connectServer(ApiUrl.WARFRAME_SOCKET);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.info("链接已被关闭:{},{}", code, reason);
        super.onClosing(webSocket, code, reason);
        if (code == 1000) {
            return;
        }
        log.info("链接已被关闭将于5秒后重新链接");
        try {
            Thread.sleep(5000);
            connectServer(ApiUrl.WARFRAME_SOCKET);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.info("链接失败：{},{}", t.getMessage(), response);
        super.onFailure(webSocket, t, response);
        log.info("链接已被关闭将于5秒后重新链接");
        try {
            Thread.sleep(5000);
            connectServer(ApiUrl.WARFRAME_SOCKET);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        SocketGlobalStates states = JSONObject.parseObject(text, SocketGlobalStates.class, JSONReader.Feature.SupportSmartMatch);
        if (!states.getEvent().equals("connected") && states.getEvent().equals("ws:update")) {
            if (states.getPacket().getLanguage().equals("en") && states.getPacket().getPlatform().equals("pc")) {
                AsyncUtils.me().execute(() -> {
                    WarframeSubscribe.isUpdated(states);
                });

            }
        }
        super.onMessage(webSocket, text);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        log.info("建立链接");
        super.onOpen(webSocket, response);
    }


}
