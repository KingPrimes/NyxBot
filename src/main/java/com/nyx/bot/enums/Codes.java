package com.nyx.bot.enums;

import lombok.Getter;

/**
 * 指令 枚举
 */
@Getter
public enum Codes {

    HELP("^HELP$|^帮助$|^指令$|^命令$|^菜单$", PermissionsEnums.USER),

    CHECK_VERSION("^检查版本$|^版本$|^运行状态$|^状态$", PermissionsEnums.USER),

    UPDATE_HTML("^更新HTML$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_RES_MARKET_ITEMS("^更新WM物品$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_RES_MARKET_RIVEN("^更新WM紫卡$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_RES_RM("^更新RM紫卡$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_RIVEN_CHANGES("^更新紫卡倾向变动$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_SISTER("^更新信条$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_WARFRAME_TAR("^更新翻译$", PermissionsEnums.SUPER_ADMIN),
    UPDATE_JAR("^自动更新$|^更新版本$|^版本更新$", PermissionsEnums.SUPER_ADMIN),

    WARFRAME_ASSAULT_PLUGIN("^突击$", PermissionsEnums.USER),
    WARFRAME_ARSON_HUNT_PLUGIN("^执刑官猎杀$|^猎杀$|^执行官$|^执政官$|^执刑官$", PermissionsEnums.USER),
    WARFRAME_VOID_PLUGIN("^奸商$", PermissionsEnums.USER),
    WARFRAME_ARBITRATION_PLUGIN("^仲裁$", PermissionsEnums.USER),
    WARFRAME_DAILY_DEALS_PLUGIN("^每日特惠$|^特惠$", PermissionsEnums.USER),
    WARFRAME_INVASIONS_PLUGIN("^入侵$", PermissionsEnums.USER),
    WARFRAME_FISSURES_PLUGIN("^裂隙$|^裂缝$", PermissionsEnums.USER),
    WARFRAME_FISSURES_EMPYREAN_PLUGIN("^九重天裂隙$|^九重天$", PermissionsEnums.USER),
    WARFRAME_FISSURES_PATH_PLUGIN("^钢铁裂隙$", PermissionsEnums.USER),
    WARFRAME_STEEL_PATH_PLUGIN("^钢铁$", PermissionsEnums.USER),
    WARFRAME_ALL_CYCLE_PLUGIN("^平原$|^夜灵平原$|^夜灵平野$", PermissionsEnums.USER),
    WARFRAME_KING_REALM_ROTATION("^轮换$|^双衍王境$", PermissionsEnums.USER),
    WARFRAME_NIGH_WAVE_PLUGIN("^电波$", PermissionsEnums.USER),
    WARFRAME_RIVEN_DIS_UPDATE_PLUGIN("^紫卡倾向变动$|^倾向变动$", PermissionsEnums.USER),
    WARFRAME_TRA_PLUGIN("^翻译", PermissionsEnums.USER),
    WARFRAME_MARKET_ORDERS_PLUGIN("^/WM|^WM|^MARKET", PermissionsEnums.USER),
    WARFRAME_MARKET_RIVEN_PLUGIN("^/WR|^WR", PermissionsEnums.USER),
    WARFRAME_RIVEN_MARKET_PLUGIN("^/RM|^RM", PermissionsEnums.USER),
    WARFRAME_CD_PLUGIN("^/CD|^CD|^赤毒", PermissionsEnums.USER),
    WARFRAME_XT_PLUGIN("^/XT|^XT|^信条", PermissionsEnums.USER),
    WARFRAME_WIKI_PLUGIN("^/WIKI", PermissionsEnums.USER),
    WARFRAME_SISTER_PLUGIN("^佩兰$|^佩兰数列$|^信条武器$", PermissionsEnums.USER),
    WARFRAME_MARKET_GOD_DUMP("^金垃圾$", PermissionsEnums.USER),
    WARFRAME_MARKET_SILVER_DUMP("^银垃圾$", PermissionsEnums.USER),
    WARFRAME_RELICS_PLUGIN("^核桃|^查核桃", PermissionsEnums.USER),
    WARFRAME_OPEN_RELICS_PLUGIN("^开核桃|^砸核桃", PermissionsEnums.USER),
    WARFRAME_RIVEN_ANALYSE("^紫卡分析|^分析紫卡", PermissionsEnums.USER),
    WARFRAME_SUBSCRIBE("^订阅 ?([0-9]+)? ?-? ?([0-9]+)?", PermissionsEnums.USER),
    WARFRAME_CANCEL_SUBSCRIBE("^取消订阅 ?([0-9]+)? ?-? ?([0-9]+)?",PermissionsEnums.USER),
    ;

    private final PermissionsEnums permissions;
    private String str;

    Codes(String s, PermissionsEnums permissions) {
        str = s;
        this.permissions = permissions;
    }

    public Codes setStr(String str) {
        this.str = str;
        return this;
    }

}
