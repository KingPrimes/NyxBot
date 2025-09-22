package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Comparator;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class KnownCalendarSeasons extends BastWorldState {

    @JsonProperty("Days")
    private List<Days> days;
    @JsonProperty("Season")
    private SeasonEnum season;
    @JsonProperty("YearIteration")
    private Integer yearIteration;
    @JsonProperty("Version")
    private Integer version;
    @JsonProperty("UpgradeAvaliabilityRequirements")
    private List<String> upgradeAvaliabilityRequirements;

    /**
     * 处理Days数据，计算自然月和日期
     */
    public void processDays() {
        if (days == null || days.isEmpty() || season == null) {
            return; // 空数据保护
        }

        SeasonEnum currentSeason = getSeason();
        int startMonth = currentSeason.getStartMonth();
        int[] seasonMonthDays = currentSeason.getMonthDays();

        // 1. 计算每个Days的自然月和日期
        for (Days day : days) {
            int seasonDay = day.getDay(); // 季节累计天数
            int monthIndex = 0;
            int remainingDays = seasonDay;

            // 计算月份索引（0-2，对应季节内的3个自然月）
            while (monthIndex < seasonMonthDays.length && remainingDays > seasonMonthDays[monthIndex]) {
                remainingDays -= seasonMonthDays[monthIndex];
                monthIndex++;
            }

            // 设置自然月和当月日期
            day.setMonth(startMonth + monthIndex);
            day.setDay(remainingDays);
        }

        // 2. 按自然月升序、日期升序排序
        days.sort(Comparator.comparingInt(Days::getMonth)
                .thenComparingInt(Days::getDay));
    }

    // conclaveChallenges
    public enum DaysTypeEnum {
        // 任务
        CET_CHALLENGE,
        // 奖励
        CET_REWARD,
        // 加成
        CET_UPGRADE,

    }

    @Getter
    public enum SeasonEnum {
        CST_FALL("秋季", 10, new int[]{31, 30, 31}),  // 秋季: 10-12月 (天数数组)
        CST_SPRING("春季", 4, new int[]{30, 31, 30}),   // 春季: 4-6月
        CST_SUMMER("夏季", 7, new int[]{31, 31, 30}),   // 夏季: 7-9月
        CST_WINTER("冬季", 1, new int[]{31, 28, 31});   // 冬季: 1-3月

        private final String name;
        private final int startMonth;  // 季节起始自然月
        private final int[] monthDays;  // 季节包含的3个自然月天数

        SeasonEnum(String name, int startMonth, int[] monthDays) {
            this.name = name;
            this.startMonth = startMonth;
            this.monthDays = monthDays;
        }
    }

    @Data
    public static class Days {
        @JsonProperty("day")
        private Integer day;
        @JsonProperty("events")
        private List<Events> events;
        @JsonProperty("month")
        private Integer month;
    }

    @Data
    public static class Events {
        @JsonProperty("type")
        private DaysTypeEnum type;
        @JsonProperty("reward")
        private String reward;
        @JsonProperty("challenge")
        private String challenge;

        @JsonProperty("challenge_info")
        private Challenge challenge_info;

        @JsonProperty("upgrade")
        private String upgrade;

        @JsonProperty("upgrade_info")
        private Upgrade upgrade_info;

    }

    @Data
    @Accessors(chain = true)
    public static class Challenge {
        String name;
        String challenge;
    }

    @Data
    @Accessors(chain = true)
    public static class Upgrade {
        String name;
        String upgrade;
    }
}
