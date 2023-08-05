package com.nyx.bot.enums;

public enum BusinessStatus {
    /**
     * 成功
     */
    SUCCESS("成功"),

    /**
     * 失败
     */
    FAIL("失败"),
    ;
    final String type;

    BusinessStatus(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
