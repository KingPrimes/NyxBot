package com.nyx.bot.modules.warframe.res.worldstate;

import com.nyx.bot.utils.TimeUtils;
import lombok.Data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 双衍王境
 */
@Data
public class DuviriCycle {


    static final long CYCLE_TIME = 36000; // 总周期时长（秒）
    static final long STATE_TIME = 7200;  // 每个阶段持续时间（秒）
    static final List<String> STATES = List.of("悲伤", "恐惧", "喜悦", "愤怒", "嫉妒");


    Instant activation;
    Instant expiry;
    String state;
    List<EndlessXpChoices> choices;
    String timeLeft;

    /**
     * 构造双衍王境循环
     *
     * @param choices 当前循环可选内容列表 从WorldState 中获取
     */
    public DuviriCycle(List<EndlessXpChoices> choices) {
        this.choices = choices;

        long nowSeconds = Instant.now().getEpochSecond();
        long cycleDelta = (nowSeconds - 52) % CYCLE_TIME;

        int stateInd = (int) (cycleDelta / STATE_TIME);
        long stateDelta = cycleDelta % STATE_TIME;
        long untilNext = STATE_TIME - stateDelta;

        this.state = STATES.get(stateInd);

        this.expiry = Instant.now().plus(untilNext, ChronoUnit.SECONDS).truncatedTo(ChronoUnit.SECONDS);
        this.activation = this.expiry.minus(STATE_TIME, ChronoUnit.SECONDS);
        this.timeLeft = TimeUtils.timeDeltaToString(this.expiry.toEpochMilli() - System.currentTimeMillis());
    }

}
