package com.nyx.bot.res.worldstate;

import com.nyx.bot.utils.TimeUtils;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 奥布山谷 轮换
 */
@Data
public class VallisCycle {

    private static final long LOOP_TIME = 1600000; // 总周期时长（毫秒）
    private static final long WARM_TIME = 400000;  // 温暖期持续时间（毫秒）
    private static final long COLD_TIME = LOOP_TIME - WARM_TIME; // 寒冷期持续时间


    private static final Instant L_START = Instant.parse("2018-11-10T08:13:48Z");


    private Instant activation;
    private Instant expiry;
    private boolean isWarm;
    private String state;
    private String timeLeft;
    private String id;
    private boolean expired;

    /**
     * 构造函数，同时执行 getCurrentCycle 的逻辑
     */
    public VallisCycle() {
        long now = System.currentTimeMillis();
        long sinceLast = (now - L_START.toEpochMilli()) % LOOP_TIME;
        long toNextFull = LOOP_TIME - sinceLast;

        String state = "寒冷";
        if (toNextFull > COLD_TIME) {
            state = "温暖";
        }

        long toNextMinor;
        if (toNextFull < COLD_TIME) {
            toNextMinor = toNextFull;
        } else {
            toNextMinor = toNextFull - COLD_TIME;
        }

        Instant timeAtNext = Instant.ofEpochMilli(now + toNextMinor);
        Instant timeAtPrevious = Instant.ofEpochMilli(
                now + toNextFull - ("温暖".equals(state) ? LOOP_TIME : COLD_TIME)
        );

        // 设置成员变量
        this.state = state;
        this.isWarm = "温暖".equals(state);
        this.expiry = timeAtNext.truncatedTo(ChronoUnit.SECONDS);
        this.activation = timeAtPrevious.truncatedTo(ChronoUnit.SECONDS);
        this.timeLeft = TimeUtils.timeDeltaToString(toNextMinor);
        this.expired = expiry.isBefore(Instant.now());
    }

}
