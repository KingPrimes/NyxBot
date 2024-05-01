package com.nyx.bot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherUtils {

    /**
     * 是否是URL地址
     */
    public static final Pattern IS_HTTP_URL = Pattern.compile("((https|http)?://)");

    /**
     * 是否是整数
     */
    public static final Pattern IS_NUMBER = Pattern.compile("^\\d+$|-\\d+$");

    /**
     * 是否是浮点数
     */
    public static final Pattern IS_DOUBLE = Pattern.compile("\\d+\\.\\d+$|-\\d+\\.\\d+$");

    /**
     * 是否是字母
     */
    public static final Pattern IS_ALPHA = Pattern.compile("[a-zA-Z]+");

    /**
     * 是否是特殊字符
     */
    public static final Pattern IS_SPECIAL_SYMBOLS = Pattern.compile("[\\!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]");

    public static final Pattern IS_ORDER_ITEM = Pattern.compile("([一-龥]+) ?\\& ?([一-龥]+)");

    public static final Pattern IS_CHINES = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static final Pattern IS_IPV4 = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");

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
        Matcher mat = IS_HTTP_URL.matcher(urls.trim());
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
        return IS_NUMBER.matcher(str).matches()
                || IS_DOUBLE.matcher(str).matches();
    }

    /**
     * 判断是否是整数
     *
     * @param str 字符串
     * @return true 整数 反之
     */
    public static boolean isNumber(String str) {
        return IS_NUMBER.matcher(str).matches();
    }

    /**
     * 判断是否是字母
     *
     * @param str 字符串
     * @return true 是字母 反之
     */
    public static boolean isAlpha(String str) {
        return str != null && IS_ALPHA.matcher(str).matches();
    }

    /**
     * 全局匹配正则表达式
     *
     * @param str   字符串
     * @param regex 正则表达式
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
     */
    public static boolean isSpecialSymbols(String str) {
        return IS_SPECIAL_SYMBOLS.matcher(str).matches();
    }

    /**
     * 对于携带特殊字符的字符串进行过滤
     */
    public static String isOrderItem(String str) {
        return IS_ORDER_ITEM.matcher(str).group();
    }

    /**
     * 匹配是否是中文
     */
    public static boolean isChines(String str) {
        return IS_CHINES.matcher(str).matches();
    }

    /**
     * 判断是否是一个合法的IPV4地址
     *
     * @param str 待匹配项
     * @return true 合规
     */
    public static boolean isIPV4(String str) {
        return IS_IPV4.matcher(str).matches();
    }

}
