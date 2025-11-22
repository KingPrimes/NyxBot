package com.nyx.bot.common.core.helper;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
public class WebSocketMessageSender {

    public void sendCompressed(Session session, String message) throws IOException {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            byte[] compressed = compressData(message);
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(compressed));
        } catch (Exception e) {
            // log as needed
            session.close();
            log.error("WebSocket 发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 压缩数据
     *
     * @param data 原始数据
     * @return 压缩后的数据
     */
    private byte[] compressData(String data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return bos.toByteArray();
    }
}