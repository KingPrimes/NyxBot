package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum FactionEnum {
    FC_MITW("墙中人"),
    FC_CORPUS("Corpus"),
    FC_CORRUPTED("堕落者"),
    FC_GRINEER("Grineer"),
    FC_INFESTATION("Infested"),
    FC_OROKIN("奥罗金"),
    FC_SENTIENT("Sentient"),
    FC_NARMER("合一众"),
    ;
    private final String name;
    FactionEnum(String name) {
        this.name = name;
    }
}
