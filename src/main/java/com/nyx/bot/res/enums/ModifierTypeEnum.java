package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum ModifierTypeEnum {
    SORTIE_MODIFIER_HAZARD_RADIATION("辐射灾害"),
    SORTIE_MODIFIER_SLASH("敌人物理强化"),
    SORTIE_MODIFIER_BOW_ONLY("仅限弓箭/弩"),
    SORTIE_MODIFIER_MAGNETIC("敌人元素强化"),
    SORTIE_MODIFIER_EXIMUS("卓越者大本营"),

    ;
    final String str;

    ModifierTypeEnum(String str) {
        this.str = str;
    }
}