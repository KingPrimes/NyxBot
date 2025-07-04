package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum ModifierTypeEnum {
    SORTIE_MODIFIER_HAZARD_RADIATION("辐射灾害"),
    SORTIE_MODIFIER_SLASH("敌人物理强化"),
    SORTIE_MODIFIER_BOW_ONLY("仅限弓箭/弩"),
    SORTIE_MODIFIER_MAGNETIC("敌人元素强化"),
    SORTIE_MODIFIER_EXIMUS("卓越者大本营"),
    SORTIE_MODIFIER_EXPLOSION("敌人元素强化"),
    SORTIE_MODIFIER_LOW_ENERGY("能量上限减少"),
    SORTIE_MODIFIER_SHOTGUN_ONLY("仅限霰弹枪"),
    SORTIE_MODIFIER_RIFLE_ONLY("仅限突击步枪"),
    SORTIE_MODIFIER_SHIELDS("敌人护盾强化")
    ;
    final String str;

    ModifierTypeEnum(String str) {
        this.str = str;
    }
}