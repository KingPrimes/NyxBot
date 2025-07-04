package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum BossEnum {
    SORTIE_BOSS_RUK("Sargas Ruk 将军"),
    SORTIE_BOSS_HYENA("鬣狗群")
    ;

    final String name;

    BossEnum(String name) {
        this.name = name;
    }
}
