package com.nyx.bot.res.enums;

import lombok.Getter;

@Getter
public enum FactionEnum {
    FC_GRINEER("Grineer"),
    FC_CORPUS("Corpus"),
    FC_INFESTATION("Infested"),
    FC_OROKIN("奥罗金"),
    FC_CORRUPTED("堕落者"),
    FC_SENTIENT("Sentient"),
    FC_NARMER("合一众"),
    FC_MURMUR("低语者"),
    FC_SCALDRA("炽蛇军"),
    FC_TECHROT("科腐者"),
    FC_DUVIRI("双衍王境"),
    FC_MITW("墙中人"),
    ;
    private final String name;

    FactionEnum(String name) {
        this.name = name;
    }
}
