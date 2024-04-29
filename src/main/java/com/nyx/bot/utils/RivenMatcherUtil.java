package com.nyx.bot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RivenMatcherUtil {
    /**
     * 获取中文
     */
    public static String getChines(String str) {
        Pattern p = Pattern.compile("[一-龥]*?\\&?[一-龥]");
        Matcher m = p.matcher(str);
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
        Pattern p = Pattern.compile("^[一-龥]*?\\&?·?[一-龥] *?[A-Za-z]*?-?[A-Za-z]*?$");
        Pattern compile = Pattern.compile("^[一-龥]*?\\&?·?[一-龥]*$");
        Matcher m = p.matcher(str.trim());
        Matcher m2 = compile.matcher(str.trim());
        return m.matches() || m2.matches();
    }

    public static String getRivenNameE(String str) {
        Pattern p = Pattern.compile("[a-zA-Z]*-?$");
        Matcher m = p.matcher(str);
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
        Pattern p = Pattern.compile(".[+-x]?\\d+(\\.\\d+)?%?.?[一-龥]*?.?（?[a-zA-Z]*?.?[一-龥]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 获取属性数值
     */
    public static Double getAttributeNum(String str) {
        String regex = "[+-]?\\d+(\\.\\d+)?%?";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        StringBuilder builder = new StringBuilder();
        while (m.find()) {
            builder.append(m.group());
            break;
        }
        return Double.valueOf(builder.toString().replace("%", "").trim());
    }

    public static Boolean whetherItIsDiscrimination(String str){
        String regex = "^x\\d+(\\.\\d+)?%?.?[一-龥]*?.?（?[a-zA-Z]*?.?[一-龥]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 获取属性名称
     */
    public static String getAttribetName(String str) {
        String regex = "[一-龥]*?（?[a-zA-Z]*?[一-龥]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
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
        Pattern p = Pattern.compile("^[一-龥]*? [a-zA-Z]*-[a-zA-Z]*$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static boolean isRivenNameEx(String str) {
        Pattern p = Pattern.compile("^[一-龥]*? ?[a-zA-Z]*-?$");
        Matcher m = p.matcher(str);
        return isRivenName(str) || m.matches();
    }

    /**
     * 判断是否是一个合法的IPV4地址
     *
     * @param str 待匹配项
     * @return true 合规
     */
    public static boolean isIPV4(String str) {
        Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
