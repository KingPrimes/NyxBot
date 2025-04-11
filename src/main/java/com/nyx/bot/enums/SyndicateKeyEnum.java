package com.nyx.bot.enums;

import lombok.Getter;

/**
 * 集团
 */
@Getter
public enum SyndicateKeyEnum {
    /**
     * 希图斯
     */
    OSTRONS("Ostrons"),

    /**
     * 英择谛
     */
    ENTRATI("Entrati"),

    /**
     * 索拉里斯
     */
    SOLARIS_UNITED("Solaris United"),

    /**
     * 科维兽
     */
    CAVIA("Cavia"),
    /**
     * 六人组
     */
    THE_HEX("The Hex"),
    /**
     * 坚守者
     */
    THE_HOLDFASTS("The Holdfasts"),
    /**
     * 均衡仲裁者
     */
    ARBITERS_OF_HEXIS("Arbiters of Hexis"),

    /**
     * 中枢苏达
     */
    CEPHALON_SUDA("Cephalon Suda"),

    OPERATIONS_SYNDICATE("Operations Syndicate"),
    /**
     * 卡尔驻军
     */
    KAHL_S_GARRISON("Kahl's Garrison"),

    /**
     * 殁世械灵
     */
    Necraloid("Necraloid"),

    /**
     * 新世间
     */
    NEW_LOKA("New Loka"),

    /**
     * 佩兰数列
     */
    PERRIN_SEQUENCE("Perrin Sequence"),

    /**
     * 夜羽
     */
    QUILLS("Quills"),

    /**
     *
     */
    THE_EMISSARY("The Emissary"),

    /**
     * 玻璃匠
     */
    GLASSMAKER("Glassmaker"),

    /**
     * 血色面纱
     */
    RED_VEIL("Red Veil"),

    /**
     * 钢铁防线
     */
    STEEL_MERIDIAN("Steel Meridian"),

    VENT_KIDS("Vent Kids"),

    /**
     * 索拉里斯之声
     */
    VOX_SOLARIS("Vox Solaris"),

    ;
    private final String key;

    SyndicateKeyEnum(String key) {
        this.key = key;
    }
}
