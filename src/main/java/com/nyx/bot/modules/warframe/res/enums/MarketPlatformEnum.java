package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

/**
 * Market 支持的平台
 */
@Getter
public enum MarketPlatformEnum {
    PC("pc"),
    PS4("ps4"),
    XBOX("xbox"),
    SWITCH("switch"),
    MOBILE("mobile"),
    ;
    private final String platform;

    MarketPlatformEnum(String platform) {
        this.platform = platform;
    }
}
