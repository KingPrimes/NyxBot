package com.nyx.bot.utils.onebot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgUtils {
    /**
     * 从消息中获取被@的QQ账号，只能获取单个
     *
     * @param msg 消息源
     * @return QQ账号
     */
    public static String getAtQQ(String msg) {
        //[1-9][0-9]{4,}
        String rex = "[1-9][0-9]{4,}";
        Pattern pat = Pattern.compile(rex.trim());
        Matcher mat = pat.matcher(msg.trim());
        String qq = null;
        while (mat.find()) {
            qq = mat.group(0);
        }
        return qq;
    }
}
