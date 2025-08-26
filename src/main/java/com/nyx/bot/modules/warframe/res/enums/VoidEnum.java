package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

@Getter
public enum VoidEnum {
    VoidT1("古纪"),
    VoidT2("前纪"),
    VoidT3("中纪"),
    VoidT4("后纪"),
    VoidT5("安魂"),
    VoidT6("全能"),
    ;
    private final String name;

    VoidEnum(String name) {
        this.name = name;
    }
}
