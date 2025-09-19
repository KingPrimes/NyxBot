package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 武器数据
 */
@SuppressWarnings("unused")
@Entity
@Table
@Data
public class Weapons {
    // 武器唯一名称
    @Id
    @JsonProperty("uniqueName")
    @NotEmpty(message = "unique_name.not.empty")
    String uniqueName;
    // 武器名称
    @JsonProperty("name")
    String name;
    // 玩家为拥有权的武器是否从资料库中隐藏
    @JsonProperty("codexSecret")
    Boolean codexSecret;
    /**
     * 武器伤害阵列
     * 依次为
     * 0 - 冲击
     * 1 - 穿刺
     * 2 - 切割
     * 3 - 火焰
     * 4 - 冰冻
     * 5 - 电击
     * 6 - 毒素
     * 7 - 爆炸
     * 8 - 辐射
     * 9 - 毒气
     * 10 - 磁力
     * 11 - 病毒
     * 12 - 腐蚀
     * 13 - 虚空
     * 14 - Tau
     * 15 - DT_CINEMATIC
     * 16 - DT_SHIELD_DRAIN
     * 17 - DT_HEALTH_DRAIN
     * 18 - DT_ENERGY_DRAIN
     * 19 - 真实
     */
    @ElementCollection(fetch = FetchType.EAGER, targetClass = Double.class)
    @JsonProperty("damagePerShot")
    List<Double> damagePerShot;
    // 武器的总伤害
    @JsonProperty("totalDamage")
    Integer totalDamage;
    // 武器的描述
    @JsonProperty("description")
    String description;
    // 武器的暴击率
    @JsonProperty("criticalChance")
    Double criticalChance;
    // 武器的暴击倍率
    @JsonProperty("criticalMultiplier")
    Double criticalMultiplier;
    // 武器的触发概率
    @JsonProperty("procChance")
    Double procChance;
    // 武器射速
    @JsonProperty("fireRate")
    Double fireRate;
    // 段位需求
    @JsonProperty("masteryReq")
    Integer masteryReq;
    // 武器的类型
    @JsonProperty("productCategory")
    ProductCategory productCategory;
    // 武器的槽位
    @JsonProperty("slot")
    Integer slot;
    // 武器的精准度
    @JsonProperty("accuracy")
    Double accuracy;
    // 裂罅MOD倾向
    @JsonProperty("omegaAttenuation")
    Double omegaAttenuation;
    // 武器的最大等级
    @JsonProperty("maxLevelCap")
    Integer maxLevelCap = 30;
    // 噪音等级
    @JsonProperty("noise")
    String noise;
    // 射击类型
    @JsonProperty("trigger")
    String trigger;
    // 弹匣容量
    @JsonProperty("magazineSize")
    Integer magazineSize;
    // 装填时间
    @JsonProperty("reloadTime")
    Double reloadTime;
    // 是否为守护武器
    @JsonProperty("sentinel")
    Boolean sentinel;
    // 多重射击
    @JsonProperty("multishot")
    Integer multishot;

    /**
     * 获取武器的伤害类型以及数值
     */
    @Transient
    public List<DamagePerShot> getDamagePerShotList() {
        String[] DamageType = {
                "冲击伤害", "穿刺伤害", "切割伤害", "火焰伤害", "冰冻伤害", "电击伤害", "毒素伤害", "爆炸伤害", "辐射伤害", "毒气伤害",
                "磁力伤害", "病毒伤害", "腐蚀伤害", "虚空伤害", "Tau伤害", "DT_CINEMATIC", "DT_SHIELD_DRAIN",
                "DT_HEALTH_DRAIN", "DT_ENERGY_DRAIN", "真实伤害"
        };
        return IntStream.range(0, damagePerShot.size())
                .filter(i -> damagePerShot.get(i) > 0)
                .mapToObj(i -> new DamagePerShot()
                        .setName(DamageType[i])
                        .setDamage(damagePerShot.get(i))
                )
                .collect(Collectors.toList());
    }

    /**
     * 武器的暴击率
     */
    public String getCriticalChanceFormat() {
        return String.format("%.2f", (criticalChance * 100));
    }

    /**
     * 武器的触发概率
     */
    public String getProcChanceFormat() {
        return String.format("%.2f", (procChance * 100));
    }

    /**
     * 获取武器射速
     */
    public String getFireRateFormat() {
        return String.format("%.2f", fireRate);
    }

    /**
     * 武器的精准度
     */
    public String getAccuracyFormat() {
        return String.format("%.2f", accuracy);
    }

    /**
     * 获取武器的裂罅MOD倾向
     */
    public String getOmegaAttenuationFormat() {
        return String.format("%.2f", omegaAttenuation);
    }

    /**
     * 装填时间
     */
    public String getReloadTimeFormat() {
        return String.format("%.2f", reloadTime);
    }


    @Getter
    public enum ProductCategory {
        Pistols("次要武器"),
        LongGuns("主要武器"),
        Melee("近战武器"),
        SpaceGuns("Archwing武器"),
        SpaceMelee("Archwing近战武器"),
        SpecialItems("显赫武器"),
        CrewShipWeapons("星舰武器"),
        SentinelWeapons("守护武器"),
        Shotguns("霰弹枪"),
        ;
        final String name;

        ProductCategory(String name) {
            this.name = name;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class DamagePerShot {
        // 伤害类型
        String name;
        // 伤害数值
        Double damage;
    }
}
