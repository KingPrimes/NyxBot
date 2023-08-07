package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
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

    public static WarframeSocket socket() {
        return socket;
    }

    WebSocket webSocket;


    public void connectServer(String url){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .connectTimeout(60,TimeUnit.SECONDS)
                .pingInterval(45,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request,this);
    }



    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.warn("链接已被关闭:{},{}",code,reason);
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.warn("链接已被关闭2:{},{}",code,reason);
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        log.warn("链接失败：{},{}",t.getMessage(),response);
        super.onFailure(webSocket, t, response);
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        SocketGlobalStates states = JSONObject.parseObject(text,SocketGlobalStates.class,JSONReader.Feature.SupportSmartMatch);
        if (!states.getEvent().equals("connected") && states.getEvent().equals("ws:update")) {
            if (states.getPacket().getLanguage().equals("en") && states.getPacket().getPlatform().equals("pc")) {
                AsyncUtils.me().execute(()->{
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
