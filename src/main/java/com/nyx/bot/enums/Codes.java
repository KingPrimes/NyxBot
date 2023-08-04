package com.nyx.bot.enums;

import lombok.Getter;

/**
 * 指令 枚举
 */
@Getter
public enum Codes {

    HELP("HELP|帮助",Permissions.USER),

    CHECK_VERSION("检查版本|版本|运行状态|状态",Permissions.ADMIN),

    UPDATE_HTML("更新HTML",Permissions.SUPER_ADMIN),
    UPDATE_RES_MARKET_ITEMS("更新WM物品",Permissions.SUPER_ADMIN),
    UPDATE_RES_MARKET_RIVEN("更新WM紫卡",Permissions.SUPER_ADMIN),
    UPDATE_RES_RM("更新RM紫卡",Permissions.SUPER_ADMIN),
    UPDATE_RIVEN_CHANGES("更新紫卡倾向变动",Permissions.SUPER_ADMIN),
    UPDATE_SISTER("更新信条",Permissions.SUPER_ADMIN),
    UPDATE_TAR("更新翻译",Permissions.SUPER_ADMIN),
    UPDATE_JAR("自动更新|更新版本|版本更新",Permissions.SUPER_ADMIN),

    TYPE_CODE("指令|命令|菜单",Permissions.USER),

    SWITCH_OPEN_WARFRAME("开启WF|开启WARFRAME|OPENWARFRAME",Permissions.ADMIN),
    SWITCH_OPEN_MUSIC("开启点歌|OPENMUSIC",Permissions.ADMIN),
    SWITCH_OPEN_IMAGE("开启色图|开启涩图|OPENIMAGE",Permissions.ADMIN),
    SWITCH_OPEN_IMAGE_NSFW("开启鉴图",Permissions.ADMIN),
    SWITCH_OPEN_CHAT_GPT("开启CHAT|OPENCHAT|ONCHAT",Permissions.SUPER_ADMIN),
    SWITCH_OPEN_EXPRESSION("开启表情|开启GIF",Permissions.ADMIN),

    SWITCH_OFF_WARFRAME("关闭WF|关闭WARFRAME|OFFWARFRAME",Permissions.ADMIN),
    SWITCH_OFF_MUSIC("关闭点歌|OFFMUSIC",Permissions.ADMIN),
    SWITCH_OFF_IMAGE("关闭色图|关闭涩图|OFFIMAGE",Permissions.ADMIN),
    SWITCH_OFF_IMAGE_NSFW("关闭鉴图",Permissions.ADMIN),
    SWITCH_OFF_CHAT_GPT("关闭CHAT|OFFCHAT",Permissions.SUPER_ADMIN),
    SWITCH_OFF_EXPRESSION("关闭表情|关闭GIF",Permissions.ADMIN),

    MUSIC("点歌|来首歌",Permissions.USER),

    IMAGE("涩图|色图|来张色图",Permissions.USER),
    IMAGE_NSFW("鉴图|看图",Permissions.USER),

    CHAT_GPT("CHAT",Permissions.USER),

    EXPRESSION_CAPO("CAPO",Permissions.USER),
    EXPRESSION_EMAIL_FUNNY("FUNNY",Permissions.USER),
    EXPRESSION_SPIRITUAL_PILLARS("精神支柱",Permissions.USER),

    WARFRAME_ASSAULT_PLUGIN("突击",Permissions.USER),
    WARFRAME_ARSON_HUNT_PLUGIN("执刑官猎杀|猎杀|执行官|执政官|执刑官",Permissions.USER),
    WARFRAME_VOID_PLUGIN("奸商",Permissions.USER),
    WARFRAME_ARBITRATION_PLUGIN("仲裁",Permissions.USER),
    WARFRAME_STEEL_PATH_PLUGIN("钢铁",Permissions.USER),
    WARFRAME_DAILY_DEALS_PLUGIN("每日特惠|特惠",Permissions.USER),
    WARFRAME_INVASIONS_PLUGIN("入侵",Permissions.USER),
    WARFRAME_FISSURES_PLUGIN("裂隙|裂缝",Permissions.USER),
    WARFRAME_FISSURES_EMPYREAN_PLUGIN("九重天裂隙|九重天",Permissions.USER),
    WARFRAME_FISSURES_PATH_PLUGIN("钢铁裂隙|钢铁",Permissions.USER),
    WARFRAME_ALL_CYCLE_PLUGIN("平原|夜灵平原|夜灵平野",Permissions.USER),
    WARFRAME_NIGH_WAVE_PLUGIN("电波",Permissions.USER),
    WARFRAME_RIVEN_DIS_UPDATE_PLUGIN("紫卡倾向变动|倾向变动",Permissions.USER),
    WARFRAME_TRA_PLUGIN("翻译",Permissions.USER),
    WARFRAME_MARKET_ORDERS_PLUGIN("/WM|WM|MARKET",Permissions.USER),
    WARFRAME_MARKET_RIVEN_PLUGIN("/WR|WR",Permissions.USER),
    WARFRAME_RIVEN_MARKET_PLUGIN("/RM|RM",Permissions.USER),
    WARFRAME_CD_PLUGIN("/CD|CD|赤毒",Permissions.USER),
    WARFRAME_XT_PLUGIN("/XT|XT|信条",Permissions.USER),
    WARFRAME_WIKI_PLUGIN("/WIKI",Permissions.USER),
    WARFRAME_SISTER_PLUGIN("佩兰|佩兰数列|信条武器",Permissions.USER),
    WARFRAME_MARKET_GOD_DUMP("金垃圾",Permissions.USER),
    WARFRAME_MARKET_SILVER_DUMP("银垃圾",Permissions.USER),
    WARFRAME_RELICS_PLUGIN("核桃|查核桃",Permissions.USER),
    WARFRAME_OPEN_RELICS_PLUGIN("开核桃|砸核桃",Permissions.USER),
    WARFRAME_RIVEN_ANALYSE("紫卡分析|分析紫卡",Permissions.USER),

    ;

    private String str;
    private final Permissions permissions;

    Codes(String s,Permissions permissions) {
        str = s;
        this.permissions = permissions;
    }

    public Codes setStr(String str) {
        this.str = str;
        return this;
    }

}
