package com.nyx.bot.enums;

import lombok.Getter;


/**
 * 用于切换线程池
 */
@Getter
public enum AsyncBeanName {

    MYASYNC("myAsync"),
    SERVICE("scheduledExecutorService"),
    InitData("initDataExecutor"),
    ;
    private final String str;

    AsyncBeanName(String s) {
        str = s;
    }
}
