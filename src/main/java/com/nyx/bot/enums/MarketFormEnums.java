package com.nyx.bot.enums;

import lombok.Getter;

@Getter
public enum MarketFormEnums {
    PC("pc"),
    XBOX("xbox"),
    PS4("ps4"),
    SWITCH("switch"),
    ;
    private final String form;

    MarketFormEnums(String form) {
        this.form = form;
    }

}
