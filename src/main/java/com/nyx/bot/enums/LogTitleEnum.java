package com.nyx.bot.enums;

import lombok.Getter;

@Getter
public enum LogTitleEnum {
    OTHER("其它"),
    PLUGIN("插件"),
    CONTROLLER("控制器"),
    ;
    private final String title;

    LogTitleEnum(String title) {
        this.title = title;
    }
}
