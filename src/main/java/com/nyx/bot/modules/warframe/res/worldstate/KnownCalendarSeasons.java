package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
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
    @JsonProperty("MonthDays")
    private Map<?, ?> monthDays;

    /**
     * 处理Days数据，计算自然月和日期
     */
    @JsonIgnore
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

    @JsonIgnore
    public KnownCalendarSeasons copy() {
        KnownCalendarSeasons copy = new KnownCalendarSeasons();
        // 拷贝基础属性
        copy.set_id(this.get_id());
        copy.setSeason(this.getSeason());
        copy.setYearIteration(this.getYearIteration());
        copy.setVersion(this.getVersion());
        copy.setActivation(this.getActivation());
        copy.setExpiry(this.getExpiry());
        copy.setUpgradeAvaliabilityRequirements(this.getUpgradeAvaliabilityRequirements());
        // 深拷贝 Days 列表
        if (this.days != null) {
            List<Days> copiedDays = new ArrayList<>(this.days.size()); // 预指定容量
            for (Days day : this.days) {
                copiedDays.add(day.copy());
            }
            copy.setDays(copiedDays);
        }
        return copy;
    }

    // conclaveChallenges
    @Getter
    public enum DaysTypeEnum {
        // 任务
        CET_CHALLENGE("任务"),
        // 奖励
        CET_REWARD("奖励"),
        // 加成
        CET_UPGRADE("加成"),
        ;
        private final String displayName;

        DaysTypeEnum(String displayName) {
            this.displayName = displayName;
        }

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
    @Accessors(chain = true)
    public static class Days {
        @JsonProperty("day")
        private Integer day;
        @JsonProperty("events")
        private List<Events> events;
        @JsonProperty("month")
        private Integer month;

        @JsonIgnore
        public Days copy() {
            Days copy = new Days();
            copy.setDay(this.day);
            copy.setMonth(this.month);
            // 深拷贝 Events 列表
            if (this.events != null) {
                List<Events> copiedEvents = new ArrayList<>(this.events.size()); // 预指定容量
                for (Events event : this.events) {
                    copiedEvents.add(event.copy());
                }
                copy.setEvents(copiedEvents);
            }
            return copy;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Events {
        @JsonProperty("type")
        private DaysTypeEnum type;
        @JsonProperty("reward")
        private String reward;
        @JsonProperty("challenge")
        private String challenge;

        @JsonProperty("challengeInfo")
        private Challenge challengeInfo;
        @JsonProperty("upgradeInfo")
        private Upgrade upgradeInfo;

        @JsonProperty("upgrade")
        private String upgrade;

        @JsonIgnore
        public Events copy() {
            Events copy = new Events();
            copy.setType(this.type);
            copy.setChallenge(this.challenge);
            copy.setReward(this.reward);
            copy.setUpgrade(this.upgrade);
            return copy;
        }

        public record Challenge(String name, String challenge) {
        }

        public record Upgrade(String name, String upgrade) {
        }

    }
}
