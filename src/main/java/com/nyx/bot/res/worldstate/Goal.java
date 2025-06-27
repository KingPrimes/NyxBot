package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.FactionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Goal extends BastWorldState {
    // 节点
    @JsonProperty("Node")
    private String node;
    // 分数 Var
    @JsonProperty("ScoreVar")
    private String scoreVar;
    // 分数标签
    @JsonProperty("ScoreLocTag")
    private String scoreLocTag;
    // 节点分数
    @JsonProperty("Count")
    private Integer count;
    // 进度百分比
    @JsonProperty("HealthPct")
    private Double healthPct;
    // 地区
    @JsonProperty("Regions")
    private List<Integer> regions;
    // 描述
    @JsonProperty("Desc")
    private String desc;
    // 提示
    @JsonProperty("Tooltip")
    private String toolTip;
    // 可选任务
    @JsonProperty("OptionalInMission")
    private Boolean optionalInMission;
    // 标签
    @JsonProperty("Tag")
    private String tag;
    // 升级
    @JsonProperty("UpgradeIds")
    private List<Id> upgradeIds;
    // 个人任务
    @JsonProperty("Personal")
    private Boolean personal;
    // 社区任务
    @JsonProperty("Community")
    private Boolean community;
    // 目标
    @JsonProperty("Goal")
    private Integer goal;
    // 奖励
    @JsonProperty("Reward")
    private Reward reward;
    // 中期目标
    @JsonProperty("InterimGoals")
    private List<Integer> interimGoals;
    // 中期奖励
    @JsonProperty("InterimRewards")
    private List<Reward> interimRewards;
    // 氏族目标
    @JsonProperty("ClanGoal")
    private List<Integer> clanGoal;
    // 派系
    @JsonProperty("Faction")
    private FactionEnum faction;
    // 图标
    @JsonProperty("Icon")
    private String icon;
    // 任务Key名称
    @JsonProperty("MissionKeyName")
    private String missionKeyName;
}
