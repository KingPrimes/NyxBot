package com.nyx.bot.modules.warframe.res.worldstate;

import com.nyx.bot.utils.TimeUtils;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 地球循环
 */
@Data
public class EarthCycle {

    // 常量定义
    private static final long CYCLE_SECONDS = 28800; // 地球周期总时长（秒）
    private static final long DAYTIME_LIMIT = 14400; // 白天最大持续时间（秒）

    // 成员变量
    private Instant activation;
    private Instant expiry;
    private boolean isDay;
    private String state;
    private String timeLeft;
    private Instant rounded;
    private Instant start;
    private String id;
    private boolean expired;


    public EarthCycle() {
        long now = System.currentTimeMillis();
        long nowSeconds = now / 1000;

        long cycleSeconds = nowSeconds % CYCLE_SECONDS;
        this.isDay = cycleSeconds < DAYTIME_LIMIT;

        long secondsLeft = DAYTIME_LIMIT - (cycleSeconds % DAYTIME_LIMIT);
        long millisLeft = secondsLeft * 1000;

        this.expiry = Instant.ofEpochMilli(now + millisLeft);

        long minutesCoef = 1000 * 60;

        this.rounded = Instant.ofEpochMilli(Math.round((float) (now + millisLeft) / minutesCoef) * minutesCoef);

        // 格式化剩余时间
        this.timeLeft = TimeUtils.timeDeltaToString(millisLeft);

        this.state = isDay ? "白昼" : "夜晚";

        // 开始时间为结束时间前 4 小时
        this.start = this.expiry.minus(4, ChronoUnit.HOURS);

        this.activation = this.start;

        this.expired = expiry.isBefore(Instant.now());
    }


}
