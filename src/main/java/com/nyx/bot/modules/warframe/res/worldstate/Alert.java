package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.FactionEnum;
import com.nyx.bot.modules.warframe.res.enums.MissionTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class Alert extends BastWorldState {
    /**
     * 是否强制解锁
     * <p>表示该警报任务是否需要完成前置任务才能参与</p>
     */
    @JsonProperty("ForceUnlock")
    Boolean forceUnlock;
    /**
     * 警报标签
     * <p>用于标识警报类型的标签，如"LotusGift"表示Lotus礼物活动</p>
     */
    @JsonProperty("Tag")
    String tag;

    /**
     * 任务信息
     * <p>包含警报任务的详细信息，如地点、任务类型、奖励等</p>
     */
    @JsonProperty("MissionInfo")
    MissionInfo missionInfo;

    /**
     * 警报任务信息类
     * <p>该类封装了警报任务的具体信息，包括任务地点、敌人类型、奖励等内容</p>
     */
    @Data
    @Accessors(chain = true)
    public static class MissionInfo {
        /**
         * 任务描述文本
         * <p>任务的描述信息，通常是本地化字符串的键值</p>
         */
        @JsonProperty("descText")
        String descText;
        /**
         * 任务难度等级
         * <p>任务的难度系数，通常为1表示普通难度</p>
         */
        @JsonProperty("difficulty")
        Integer difficulty;
        /**
         * 敌人规格
         * <p>指定任务中出现的敌人类型配置文件路径</p>
         */
        @JsonProperty("enemySpec")
        String enemySpec;
        /**
         * 敌人阵营
         * <p>任务中敌人的所属阵营，如Corpus、Grineer等</p>
         */
        @JsonProperty("faction")
        FactionEnum faction;
        /**
         * 关卡覆盖配置
         * <p>指定任务使用的特殊关卡配置文件路径</p>
         */
        @JsonProperty("levelOverride")
        String levelOverride;
        /**
         * 任务地点
         * <p>任务所在的星图节点，如"SolNode123"</p>
         */
        @JsonProperty("location")
        String location;
        /**
         * 最大敌人等级
         * <p>任务中出现的敌人的最高等级</p>
         */
        @JsonProperty("maxEnemyLevel")
        Integer maxEnemyLevel;
        /**
         * 最大波次数量
         * <p>防御或生存类任务的最大波次数量</p>
         */
        @JsonProperty("maxWaveNum")
        Integer maxWaveNum;
        /**
         * 最小敌人等级
         * <p>任务中出现的敌人的最低等级</p>
         */
        @JsonProperty("minEnemyLevel")
        Integer minEnemyLevel;

        /**
         * 任务类型
         * <p>任务的类型，如生存(MT_SURVIVAL)、防御(MT_DEFENSE)、挖掘(MT_EXCAVATE)等</p>
         */
        @JsonProperty("missionType")
        MissionTypeEnum missionType;

        /**
         * 任务奖励
         * <p>完成任务后可获得的奖励信息</p>
         */
        @JsonProperty("missionReward")
        Reward missionReward;

        /**
         * 任务奖励类
         * <p>该类封装了完成警报任务后可获得的奖励信息</p>
         */
        @Data
        @Accessors(chain = true)
        public static class Reward {
            /**
             * 奖励的现金数额
             * <p>完成任务后可获得的现金奖励数量</p>
             */
            @JsonProperty("credits")
            Integer credits;
            /**
             * 奖励的物品列表
             * <p>完成任务后可获得的物品，以物品路径形式表示</p>
             */
            @JsonProperty("items")
            List<String> items;
        }
    }
}
