package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum ModifierTypeEnum {
    SORTIE_MODIFIER_HAZARD_RADIATION("辐射灾害"),
    SORTIE_MODIFIER_SLASH("敌人物理强化"),
    SORTIE_MODIFIER_BOW_ONLY("仅限弓箭/弩")
    ;
    final String str;

    ModifierTypeEnum(String str) {
        this.str = str;
    }
}