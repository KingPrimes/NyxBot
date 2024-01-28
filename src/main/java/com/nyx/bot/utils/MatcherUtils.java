package com.nyx.bot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherUtils {

    /**
     * 正则判断
     *
     * @param str   字符串
     * @param regex 正则表达式
     * @return 是否符合正则表达式
     */
    public static boolean matcher(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 正则判断
     *
     * @param str   字符串
     * @param regex 正则表达式
     * @return 是否符合正则表达式
     */
    public static boolean matcherIgnoreCase(String str, String regex) {
        Pattern p = Pattern.compile("(?i)" + regex);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 判断字符串是否为URL
     *
     * @param urls 需要判断的String类型url
     * @return true:是URL；false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean distro = false;
        String regex = "((https|http)?://)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//对比
        Matcher mat = pat.matcher(urls.trim());
        while (mat.find()) {
            distro = true;
        }
        return distro;
    }

    /**
     * 判断是否是数字
     *
     * @param str 字符串
     * @return true 是数字 反之
     */
    public static boolean isNumberAndDouble(String str) {
        //是否整数
        Pattern num = Pattern.compile("^\\d+$|-\\d+$");
        //小数
        Pattern dou = Pattern.compile("\\d+\\.\\d+$|-\\d+\\.\\d+$");

        return num.matcher(str).matches()
                || dou.matcher(str).matches();
    }

    /**
     * 判断是否是整数
     *
     * @param str 字符串
     * @return true 整数 反之
     */
    public static boolean isNumber(String str) {
        //是否整数
        Pattern num = Pattern.compile("^\\d+$|-\\d+$");
        return num.matcher(str).matches();
    }

    /**
     * 判断是否是字母
     *
     * @param str 字符串
     * @return true 是字母 反之
     */
    public static boolean isAlpha(String str) {
        return str != null && str.matches("[a-zA-z]+");
    }

    /**
     * 全局匹配正则表达式
     *
     * @param str   字符串
     * @param regex 正则表达式
     * @return
     */
    public static boolean regexG(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 正则表达式字符串替换
     *
     * @param str       字符串
     * @param regex     正则表达式
     * @param newString 新的替换字符串
     * @return 返回替换后的字符串
     */
    public static String regReplace(String str, String regex, String newString) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.replaceAll(newString);
    }

    /**
     * 正则匹配特殊符号
     *
     * @param str
     * @return
     */
    public static boolean isSpecialSymbols(String str) {
        String regex = "[" +
                "\\!@#$%^&*()_+" +
                "\\-=\\[\\]{};':\"\\\\|,.<>/?`~" +
                "]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 对于携带特殊字符的字符串进行过滤
     */
    public static String isOrderItem(String str) {
        Pattern p = Pattern.compile("([一-龥]+) ?\\& ?([一-龥]+)");
        Matcher m = p.matcher(str);
        while (m.find()) {
            return m.group();
        }
        return "";
    }

}
