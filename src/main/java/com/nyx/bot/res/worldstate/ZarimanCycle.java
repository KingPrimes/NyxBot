package com.nyx.bot.res.worldstate;

import com.nyx.bot.utils.TimeUtils;
import lombok.Data;

import java.time.Instant;

/**
 * 扎的曼轮换
 */
@Data
public class ZarimanCycle {

    private static final long CORPUS_TIME_MILLIS = 1655182800000L; // Corpus 起始时间（毫秒）
    private static final long FULL_CYCLE = 18000000; // 完整周期时长（毫秒）
    private static final long STATE_MAXIMUM = 9000000; // 每个阶段最大持续时间（毫秒）


    private Instant activation;
    private Instant expiry;
    private boolean isCorpus;
    private String state;
    private String timeLeft;
    private String id;
    private boolean expired;

    /**
     * 构造函数，同时执行 getCurrentZarimanCycle 的逻辑
     *
     * @param bountiesEndDate 当前 Zariman Bounty 的结束时间
     */
    public ZarimanCycle(Instant bountiesEndDate) {
        long now = System.currentTimeMillis();

        // 计算剩余时间
        long bountiesClone = bountiesEndDate.toEpochMilli() - 5000;
        long millisLeft = bountiesClone - now;

        // 计算当前处于哪个周期段
        long cycleTimeElapsed = (((bountiesClone - CORPUS_TIME_MILLIS) % FULL_CYCLE + FULL_CYCLE) % FULL_CYCLE);
        long cycleTimeLeft = FULL_CYCLE - cycleTimeElapsed;
        this.isCorpus = cycleTimeLeft > STATE_MAXIMUM;

        // 设置状态字符串
        this.state = isCorpus ? "Grineer" : "Corpus";

        // 时间格式化处理
        long minutesCoef = 1000 * 60;
        this.expiry = Instant.ofEpochMilli(Math.round((float) (now + millisLeft) / minutesCoef) * minutesCoef);
        this.activation = expiry.minusMillis((int) STATE_MAXIMUM);

        // 时间剩余字符串
        this.timeLeft = TimeUtils.timeDeltaToString(millisLeft);


        this.expired = expiry.isBefore(Instant.now());
    }

}
