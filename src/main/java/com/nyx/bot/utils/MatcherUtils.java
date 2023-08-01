package com.nyx.bot.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherUtils {

    /**
     * 正则判断
     * @param str 字符串
     * @param regex 正则表达式
     * @return 是否符合正则表达式
     */
    public static boolean matcher(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }
}
