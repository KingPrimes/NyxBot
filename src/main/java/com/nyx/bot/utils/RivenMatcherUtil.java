package com.nyx.bot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RivenMatcherUtil {

    private static final Pattern IS_CHINES = Pattern.compile("[一-龥]*? ?·?\\&? ?[一-龥]");

    private static final Pattern WEAPONS_NAME = Pattern.compile("^[一-龥]*?\\&?·?[一-龥]*$");

    private static final Pattern WEAPONS_NAME_ = Pattern.compile("^[一-龥]*?\\&?·?[一-龥] *?[A-Za-z]*?-?[A-Za-z]*?$");

    private static final Pattern RIVEN_NAME = Pattern.compile("[a-zA-Z]*-?$");

    private static final Pattern IS_ATTRIBUTE = Pattern.compile(".[+-x]?\\d+(\\.\\d+)?%?.?[一-龥]*?.?（?[a-zA-Z]*?.?[一-龥]+$");

    private static final Pattern ATTRIBUTE_NUM = Pattern.compile("[+-]?\\d+(\\.\\d+)?%?");

    private static final Pattern IS_DISCRIMINATION = Pattern.compile("^x\\d+(\\.\\d+)?%?.?[一-龥]*?.?（?[a-zA-Z]*?.?[一-龥]+$");

    private static final Pattern IS_ATTRIBUTE_NAME = Pattern.compile("[一-龥]*?（?[a-zA-Z]*?[一-龥]+$");

    private static final Pattern IS_RIVEN_NAME = Pattern.compile("^[一-龥]*? [a-zA-Z]*-[a-zA-Z]*$");

    private static final Pattern IS_RIVEN_NAME_EX = Pattern.compile("^[一-龥]*? ?[a-zA-Z]*-?$");

    /**
     * 获取中文
     */
    public static String getChines(String str) {
        Matcher m = IS_CHINES.matcher(str);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group());
        }
        return builder.toString();
    }

    /**
     * 判断是否是武器名称
     */
    public static boolean isWeaponsName(String str) {
        return WEAPONS_NAME_.matcher(str.trim()).matches() || WEAPONS_NAME.matcher(str.trim()).matches();
    }

    public static String getRivenNameE(String str) {
        Matcher m = RIVEN_NAME.matcher(str);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group());
        }
        return builder.toString();
    }

    /**
     * 获取紫卡名称
     */
    public static String getRivenName(String str) {
        Pattern p = Pattern.compile("[a-zA-Z]*-[a-zA-Z]*$");
        Matcher m = p.matcher(str);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group());
        }
        return builder.toString();
    }

    /**
     * 判断是否是属性词条
     */
    public static boolean isAttribute(String str) {
        return IS_ATTRIBUTE.matcher(str).matches();
    }

    /**
     * 获取属性数值
     */
    public static Double getAttributeNum(String str) {
        Matcher m = ATTRIBUTE_NUM.matcher(str);
        double v = 0.0;
        while (m.find()) {
            v = Double.parseDouble(m.group().replace("%", "").trim());
        }
        return v;
    }

    public static Boolean whetherItIsDiscrimination(String str) {
        return IS_DISCRIMINATION.matcher(str).matches();
    }

    /**
     * 获取属性名称
     */
    public static String getAttributeName(String str) {
        Matcher m = IS_ATTRIBUTE_NAME.matcher(str);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group());
        }
        return builder.toString().trim();
    }

    /**
     * 检测是否是紫卡名称
     */
    public static boolean isRivenName(String str) {
        return IS_RIVEN_NAME.matcher(str).matches();
    }

    public static boolean isRivenNameEx(String str) {
        return isRivenName(str) || IS_RIVEN_NAME_EX.matcher(str).matches();
    }

}
