package com.nyx.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
public class StatusService {

    private static final File uuidFile = new File("./data/status.json");

    private static final String baseStr = "abcdefhiklmnorstuvwxz";
    private static final String ROOT = "http://localhost/status?uuid=";

    @PostConstruct
    public void init() {
        pushDay();
    }

    /**
     * 每日推送一次
     */
    @Scheduled(cron = "0 0 0/6 * * ?")
    public void pushDay() {
        if (HandOff.getConfig().getIsStatusEnable()) {
            StatusConfig config = getConfig();
            if (DateUtils.betweenDay(System.currentTimeMillis(), config.getLastPush()) > 0) {
                checkUUID();
                config = getConfig();
                HttpUtils.sendGet(ROOT + config.getUuid());
                config.setLastPush(System.currentTimeMillis());
                saveConfig(config);
            }
        }
    }

    /**
     * 检查 uuid 是否有效
     * 规则: 转hex(12位随机字符串#时间戳每位^2(用#补足到32位)) = 64位
     */
    private void checkUUID() {
        if (!HandOff.getConfig().getIsStatusEnable()) return;
        if (!uuidFile.exists()) createUUID();
        String uuidContent = getConfig().getUuid();
        if (uuidContent.isBlank() || uuidContent.length() != 64 || getTimeFromUUID(uuidContent) < 0) {
            uuidFile.delete();
            createUUID();
        }
    }

    private String createUUID() {
        if (!HandOff.getConfig().getIsStatusEnable()) return "";
        String[] zd = (baseStr.toUpperCase() + baseStr.toLowerCase()).split("");
        Random random = new Random();
        long time = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        StringBuilder sb_t = new StringBuilder();
        // 生成随机字符串
        for (int i = 0; i < 5; i++) {
            sb.append(zd[random.nextInt(zd.length)]);
        }
        // 标识时间开始
        sb.append("#");
        for (String t : String.valueOf(time).split("")) {
            int c = Integer.parseInt(t) ^ 2;
            if (c < 10) sb_t.append("0");
            sb_t.append(c);
        }
        // 开始用#补足到32位
        while (sb.length() + sb_t.length() < 32) {
            sb_t.append("#");
        }
        // 整合
        sb.append(sb_t);
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        StringBuilder uuid = new StringBuilder();
        // 转为hex
        for (byte b : bytes) {
            uuid.append(Integer.toHexString(Integer.parseInt(String.valueOf(b))));
        }
        StatusConfig cfg = getConfig();
        cfg.setUuid(uuid.toString());
        cfg.setLastPush(-1L);
        saveConfig(cfg);
        return uuid.toString();
    }

    private long getTimeFromUUID(String uuid) {
        if (!HandOff.getConfig().getIsStatusEnable()) return -1;
        if (uuid.length() == 64) {
            String tData = uuid.substring(10);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // hex转byte
            for (int i = 0; i < tData.length() - 2; i += 2) {
                bos.write(Integer.parseInt(tData.substring(i, i + 2), 16));
            }
            StringBuilder timeStr = new StringBuilder();
            String tStr = bos.toString().replace("#", "");
            // time转
            for (int i = 0; i < tStr.length() - 2; i += 2) {
                timeStr.append(Integer.parseInt(tStr.substring(i, i + 2)) ^ 2);
            }
            return Long.parseLong(timeStr.toString()) * 10;
        }
        return -1;
    }

    public StatusConfig getConfig() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(uuidFile, StatusConfig.class);
        } catch (IOException e) {
            return StatusConfig.builder().uuid("").lastPush(0L).build();
        }
    }

    public void saveConfig(StatusConfig cfg) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (!uuidFile.getParentFile().exists()) uuidFile.getParentFile().mkdirs();
            if (!uuidFile.exists()) uuidFile.createNewFile();
            FileUtils.writeFile(uuidFile.getAbsolutePath(), mapper.writeValueAsString(cfg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusConfig implements Serializable {
        private String uuid;
        private long lastPush;
    }
}
