package com.nyx.bot.enums;

public enum RivenTrendEnum {
    RIVEN_TREND_1("●○○○○"),
    RIVEN_TREND_2("●●○○○"),
    RIVEN_TREND_3("●●●○○"),
    RIVEN_TREND_4("●●●●○"),
    RIVEN_TREND_5("●●●●●"),

    ;

    final String doc;

    RivenTrendEnum(String doc) {
        this.doc = doc;
    }


    /**
     * 获取紫卡倾向点数
     */
    public static String getRivenTrendDot(double dot) {
        if (dot < 0.7) {
            return RIVEN_TREND_1.doc;
        } else if (dot >= 0.7 && dot < 0.9) {
            return RIVEN_TREND_2.doc;
        } else if (dot >= 0.9 && dot < 1.15) {
            return RIVEN_TREND_3.doc;
        } else if (dot >= 1.15 && dot < 1.3) {
            return RIVEN_TREND_4.doc;
        } else if (dot >= 1.3) {
            return RIVEN_TREND_5.doc;
        }
        return null;
    }
}
