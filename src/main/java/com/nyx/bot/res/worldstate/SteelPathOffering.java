package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.utils.TimeUtils;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Data
public class SteelPathOffering {
    private static final LocalDateTime START_DATE = LocalDateTime.of(2020, 11, 16, 0, 0, 0);

    // Getters
    private SteelPathReward currentReward;
    private SteelPathReward nextReward;
    private LocalDateTime activation;
    private LocalDateTime expiry;
    private String remaining;
    @JsonIgnore
    private List<SteelPathReward> rotation;
    @JsonIgnore
    private List<SteelPathReward> evergreens;
    @JsonIgnore
    private Incursions incursions;

    public SteelPathOffering() {
        // 初始化数据
        initializeSteelPathData();

        // 计算当前周期索引
        long secondsSinceStart = ChronoUnit.SECONDS.between(START_DATE, LocalDateTime.now(ZoneOffset.UTC));
        long eightWeeks = 4838400; // 8周的秒数
        long sevenDays = 604800;   // 7天的秒数

        long ind = (secondsSinceStart % eightWeeks) / sevenDays;

        // 设置当前奖励
        if (!this.rotation.isEmpty()) {
            this.currentReward = this.rotation.get((int) (ind % this.rotation.size()));
            this.nextReward = this.rotation.get((int) ((ind + 1) % this.rotation.size()));
        }

        // 设置激活和过期时间
        this.activation = getFirstDayOfWeek();
        this.expiry = getLastDayOfWeek();

        // 计算剩余时间
        this.remaining = TimeUtils.timeDeltaToString(ChronoUnit.MILLIS.between(LocalDateTime.now(ZoneOffset.UTC), this.expiry));

        // 设置入侵信息
        LocalDateTime startOfDay = getStartOfDay();
        this.incursions = new Incursions(
                "spi:" + startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli(),
                startOfDay,
                getEndOfDay()
        );
    }

    /**
     * 初始化 Steel Path 数据
     */
    private void initializeSteelPathData() {
        // Rotation 数据
        this.rotation = new ArrayList<>();
        this.rotation.add(new SteelPathReward("Umbra Forma 蓝图", 150));
        this.rotation.add(new SteelPathReward("50,000 赤毒", 55));
        this.rotation.add(new SteelPathReward("组合枪裂罅 Mod", 75));
        this.rotation.add(new SteelPathReward("3x Forma", 75));
        this.rotation.add(new SteelPathReward("Zaw 裂罅 Mod", 75));
        this.rotation.add(new SteelPathReward("30,000 内融核心", 150));
        this.rotation.add(new SteelPathReward("步枪裂罅 Mod", 75));
        this.rotation.add(new SteelPathReward("霰弹枪裂罅 Mod", 75));

        // Evergreen 数据
        this.evergreens = new ArrayList<>();
        this.evergreens.add(new SteelPathReward("裂罅破解器", 20));
        this.evergreens.add(new SteelPathReward("军神护肩 蓝图", 15));
        this.evergreens.add(new SteelPathReward("军神胸甲 蓝图", 25));
        this.evergreens.add(new SteelPathReward("军神头盔 蓝图", 20));
        this.evergreens.add(new SteelPathReward("军神护胫 蓝图", 25));
        this.evergreens.add(new SteelPathReward("10,000 赤毒", 15));
        this.evergreens.add(new SteelPathReward("主要武器赋能槽连接器", 15));
        this.evergreens.add(new SteelPathReward("次要武器赋能槽连接器", 15));
        this.evergreens.add(new SteelPathReward("遗物组合包", 15));
        this.evergreens.add(new SteelPathReward("架式 Forma 蓝图", 10));
        this.evergreens.add(new SteelPathReward("三轨幻纹", 3));
        this.evergreens.add(new SteelPathReward("颅骨幻纹", 85));
        this.evergreens.add(new SteelPathReward("制衡", 35));
        this.evergreens.add(new SteelPathReward("Teshin 摇头娃娃", 35));
        this.evergreens.add(new SteelPathReward("Gauss 战斗姿态浮印", 15));
        this.evergreens.add(new SteelPathReward("Grendel 战斗姿态浮印", 15));
        this.evergreens.add(new SteelPathReward("Protea 战斗姿态浮印", 15));
        this.evergreens.add(new SteelPathReward("Orokin 茶具", 15));
        this.evergreens.add(new SteelPathReward("Xaku 战斗姿态浮印", 15));
    }

    /**
     * 获取一周的第一天（周一）
     */
    private LocalDateTime getFirstDayOfWeek() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate firstDay = today.minusDays((today.getDayOfWeek().getValue() + 5) % 7);
        return firstDay.atStartOfDay().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * 获取一周的最后一天（周日）
     */
    private LocalDateTime getLastDayOfWeek() {
        LocalDateTime firstDay = getFirstDayOfWeek();
        return firstDay.plusDays(6).withHour(23).withMinute(59).withSecond(59).withNano(0);
    }

    /**
     * 获取当天的开始时间
     */
    private LocalDateTime getStartOfDay() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * 获取当天的结束时间
     */
    private LocalDateTime getEndOfDay() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return today.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toLocalDateTime();
    }


    /**
     * 内部类表示 Steel Path 奖励
     */
    @Getter
    public static class SteelPathReward {
        // Getters
        private final String name;
        private final int cost;

        public SteelPathReward(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }

        @Override
        public String toString() {
            return name + " (" + cost + ")";
        }
    }

    /**
     * 内部类表示入侵信息
     */
    @Getter
    public static class Incursions {
        // Getters
        private final String id;
        private final LocalDateTime activation;
        private final LocalDateTime expiry;

        public Incursions(String id, LocalDateTime activation, LocalDateTime expiry) {
            this.id = id;
            this.activation = activation;
            this.expiry = expiry;
        }

    }
}
