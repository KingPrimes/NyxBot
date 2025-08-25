package com.nyx.bot.modules.warframe.res.worldstate;

import com.nyx.bot.utils.TimeUtils;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 夜灵平原
 */
@Data
public class CetusCycle {
    private static final long NIGHT_TIME = 3000; // 夜晚持续时间（秒）
    private static final Map<String, Long> MAXIMUMS = new HashMap<>();

    static {
        MAXIMUMS.put("白昼", 6000000L); // 白天最大持续时间（毫秒）
        MAXIMUMS.put("夜晚", 3000000L); // 夜晚最大持续时间（毫秒）
    }

    Boolean isDay;
    Instant expiry;
    Instant activation;
    String state;
    String timeLeft;

    /**
     * 获取当前CetusCycle
     *
     * @param bountiesEndDate SyndicateMissions.Tag为 CetusSyndicate 的 Expiry 中的数据
     */
    public CetusCycle(Instant bountiesEndDate) {
        Instant now = Instant.now();
        Instant bountiesClone = bountiesEndDate.truncatedTo(ChronoUnit.SECONDS);

        long millisLeft = java.time.Duration.between(now, bountiesClone).toMillis();
        long secondsToNightEnd = Math.round((double) millisLeft / 1000);
        boolean dayTime = secondsToNightEnd > NIGHT_TIME;

        long secondsRemainingInCycle = dayTime ? secondsToNightEnd - NIGHT_TIME : secondsToNightEnd;
        millisLeft = secondsRemainingInCycle * 1000;
        long minutesCof = 1000 * 60;
        Instant expiry = now.plusMillis(Math.round((double) millisLeft / minutesCof) * minutesCof);
        String state = dayTime ? "白昼" : "夜晚";

        this.setActivation(expiry.minusMillis(MAXIMUMS.get(state)));
        this.setExpiry(expiry);
        this.setIsDay(dayTime);
        this.setState(state);
        this.setTimeLeft(TimeUtils.timeDeltaToString(millisLeft));
    }

}
