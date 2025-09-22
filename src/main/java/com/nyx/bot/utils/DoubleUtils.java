package com.nyx.bot.utils;

public class DoubleUtils {
    /**
     * 保留一位小数 四舍五入
     *
     * @param d 浮点数
     * @return 保留一位小数得字符串, 包含符号-
     */
    public static Double formatDouble4(double d) {
        return (double) Math.round(d * 10) / 10;
    }
}
