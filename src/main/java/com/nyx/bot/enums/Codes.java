package com.nyx.bot.enums;

import lombok.Getter;

import static com.nyx.bot.enums.CommandConstants.*;

/**
 * 指令 枚举
 */
@Getter
public enum Codes {
    HELP(HELP_CMD, PermissionsEnums.USER, "显示所有可用指令"),

    CHECK_VERSION(CHECK_VERSION_CMD, PermissionsEnums.USER, "查看机器人运行状态"),

    UPDATE_WARFRAME_RES_MARKET_ITEMS(UPDATE_WARFRAME_RES_MARKET_ITEMS_CMD, PermissionsEnums.SUPER_ADMIN, "更新Warframe.Market物品数据"),
    UPDATE_WARFRAME_RES_MARKET_RIVEN(UPDATE_WARFRAME_RES_MARKET_RIVEN_CMD, PermissionsEnums.SUPER_ADMIN, "更新Warframe.Market紫卡数据"),
    UPDATE_WARFRAME_SISTER(UPDATE_WARFRAME_SISTER_CMD, PermissionsEnums.SUPER_ADMIN, "更新信条武器数据"),
    UPDATE_WARFRAME_TAR(UPDATE_WARFRAME_TAR_CMD, PermissionsEnums.SUPER_ADMIN, "更新游戏翻译数据"),

    WARFRAME_ALERTS_PLUGIN(WARFRAME_ALERTS_CMD, PermissionsEnums.USER, "查看当前警报任务"),
    WARFRAME_SORTIES_PLUGIN(WARFRAME_SORTIES_CMD, PermissionsEnums.USER, "查看当前突击任务"),
    WARFRAME_LITE_SORITE_PLUGIN(WARFRAME_LITE_SORITE_CMD, PermissionsEnums.USER, "查看执政官突击任务"),
    WARFRAME_VOID_PLUGIN(WARFRAME_VOID_CMD, PermissionsEnums.USER, "查看虚空商人货物信息"),
    WARFRAME_ARBITRATION_EX_PLUGIN(WARFRAME_ARBITRATION_EX_CMD, PermissionsEnums.USER, "查看有价值的仲裁任务列表"),
    WARFRAME_ARBITRATION_PLUGIN(WARFRAME_ARBITRATION_CMD, PermissionsEnums.USER, "查看当前进行中的仲裁任务"),
    WARFRAME_DAILY_DEALS_PLUGIN(WARFRAME_DAILY_DEALS_CMD, PermissionsEnums.USER, "查看今日每日特惠物品"),
    WARFRAME_INVASIONS_PLUGIN(WARFRAME_INVASIONS_CMD, PermissionsEnums.USER, "查看当前入侵任务进度"),
    WARFRAME_ACTIVE_MISSION_PLUGIN(WARFRAME_ACTIVE_MISSION_CMD, PermissionsEnums.USER, "查看当前虚空裂隙任务"),
    WARFRAME_VOID_STORMS_PLUGIN(WARFRAME_VOID_STORMS_CMD, PermissionsEnums.USER, "查看九重天虚空裂隙"),
    WARFRAME_ACTIVE_MISSION_PATH_PLUGIN(WARFRAME_ACTIVE_MISSION_PATH_CMD, PermissionsEnums.USER, "查看钢铁之路裂隙任务"),
    WARFRAME_STEEL_PATH_PLUGIN(WARFRAME_STEEL_PATH_CMD, PermissionsEnums.USER, "查看钢铁之路兑换奖励"),
    WARFRAME_ALL_CYCLE_PLUGIN(WARFRAME_ALL_CYCLE_CMD, PermissionsEnums.USER, "查看开放世界昼夜循环"),
    WARFRAME_SYNDICATE_OSTRONS(WARFRAME_SYNDICATE_OSTRONS_CMD, PermissionsEnums.USER, "查看希图斯赏金任务"),
    WARFRAME_SYNDICATE_ENTRATI(WARFRAME_SYNDICATE_ENTRATI_CMD, PermissionsEnums.USER, "查看英择谛赏金任务"),
    WARFRAME_SYNDICATE_SOLARIS_UNITED(WARFRAME_SYNDICATE_SOLARIS_UNITED_CMD, PermissionsEnums.USER, "查看索拉里斯联盟赏金"),
    WARFRAME_DUVIRI_CYCLE(WARFRAME_DUVIRI_CYCLE_CMD, PermissionsEnums.USER, "查看双衍王境轮换信息"),
    WARFRAME_NIGH_WAVE_PLUGIN(WARFRAME_NIGH_WAVE_CMD, PermissionsEnums.USER, "查看当前午夜电波挑战"),
    WARFRAME_RIVEN_DIS_UPDATE_PLUGIN(WARFRAME_RIVEN_DIS_UPDATE_CMD, PermissionsEnums.USER, "查看紫卡倾向变动记录"),
    WARFRAME_TRA_PLUGIN(WARFRAME_TRA_CMD, PermissionsEnums.USER, "查询中英文翻译对照"),
    WARFRAME_MARKET_RIVEN_PLUGIN(WARFRAME_MARKET_RIVEN_CMD, PermissionsEnums.USER, "查询Warframe.Market紫卡拍卖"),
    WARFRAME_MARKET_ORDERS_PLUGIN(WARFRAME_MARKET_ORDERS_CMD, PermissionsEnums.USER, "查询Warframe.Market物品订单"),
    WARFRAME_RIVEN_MARKET_PLUGIN(WARFRAME_RIVEN_MARKET_CMD, PermissionsEnums.USER, "紫卡市场快捷查询"),
    WARFRAME_LICHES_PLUGIN(WARFRAME_LICHES_CMD, PermissionsEnums.USER, "查询赤毒武器拍卖价格"),
    WARFRAME_SISTERS_PLUGIN(WARFRAME_SISTERS_CMD, PermissionsEnums.USER, "查询信条武器拍卖价格"),
    WARFRAME_THE_PERLIN_SEQUENCE_PLUGIN(WARFRAME_THE_PERLIN_SEQUENCE_CMD, PermissionsEnums.USER, "查信条/赤毒武器列表"),
    WARFRAME_MARKET_GOD_DUMP(WARFRAME_MARKET_GOD_DUMP_CMD, PermissionsEnums.USER, "查看虚空商人金垃圾价格"),
    WARFRAME_MARKET_SILVER_DUMP(WARFRAME_MARKET_SILVER_DUMP_CMD, PermissionsEnums.USER, "查看虚空商人银垃圾价格"),
    WARFRAME_RELICS_PLUGIN(WARFRAME_RELICS_CMD, PermissionsEnums.USER, "查询遗物名称或奖励物品"),
    WARFRAME_OPEN_RELICS_PLUGIN(WARFRAME_OPEN_RELICS_CMD, PermissionsEnums.USER, "查询遗物出金率信息"),
    WARFRAME_RIVEN_ANALYSE(WARFRAME_RIVEN_ANALYSE_CMD, PermissionsEnums.USER, "紫卡属性计算与分析"),
    WARFRAME_SUBSCRIBE(WARFRAME_SUBSCRIBE_CMD, PermissionsEnums.USER, "订阅游戏事件推送通知"),
    WARFRAME_UNSUBSCRIBE(WARFRAME_UNSUBSCRIBE_CMD, PermissionsEnums.USER, "取消订阅游戏事件通知"),
    WARFRAME_KNOWN_CALENDAR_SEASONS_PLUGIN(WARFRAME_KNOWN_CALENDAR_SEASONS_CMD, PermissionsEnums.USER, "查看1999年历季节信息");


    private final PermissionsEnums permissions;
    private final String comm;
    private final String desc;

    Codes(String comm, PermissionsEnums permissions, String desc) {
        this.comm = comm;
        this.permissions = permissions;
        this.desc = desc;
    }

}
