package com.nyx.bot.utils.onebot;

import com.nyx.bot.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CqParse {
    private static String CQ;

    public static CqParse build(String str) {
        CQ = str;
        return new CqParse();
    }

    /**
     * 获取表情ID
     *
     * @return 表情ID集合
     */
    public List<Integer> getCqFace() {
        Pattern p = Pattern.compile("CQ:face,id=[0-9]+");
        Matcher m = p.matcher(CQ);
        List<Integer> ids = new ArrayList<>();
        while (m.find()) {
            Pattern pa = Pattern.compile("[0-9]+");
            Matcher ma = pa.matcher(m.group());
            while (ma.find()) {
                ids.add(Integer.valueOf(ma.group()));
            }
        }
        return ids;
    }

    /**
     * 获取图片Url地址
     *
     * @return Url地址集合
     */
    public List<String> getCqImageUrl() {
        Pattern p = Pattern.compile("CQ:image,(.*?)url=(.*?)?term");
        Matcher m = p.matcher(CQ);
        List<String> ids = new ArrayList<>();
        while (m.find()) {
            Pattern pa = Pattern.compile("[a-zA-Z]+://[^\\s]*term");
            Matcher ma = pa.matcher(m.group());
            while (ma.find()) {
                ids.add(ma.group());
            }
        }
        return ids;
    }

    /**
     * 获取图片的MD5
     *
     * @return MD5集合
     */
    public List<String> getCqImageMD5() {
        Pattern p = Pattern.compile("CQ:image,file=(.*?),");
        Matcher m = p.matcher(CQ);
        List<String> ids = new ArrayList<>();
        while (m.find()) {
            ids.add(StringUtils.getSubString(m.group(), "file=", ".image,"));
        }
        return ids;
    }


    /**
     * 获取AT
     *
     * @return AT的QQ号集合
     */
    public List<Long> getCqAt() {
        Pattern p = Pattern.compile("CQ:at,qq=[1-9][0-9]{4,}");
        Matcher m = p.matcher(CQ);
        List<Long> ids = new ArrayList<>();
        while (m.find()) {
            Pattern pa = Pattern.compile("[1-9][0-9]{4,}");
            Matcher ma = pa.matcher(m.group());
            while (ma.find()) {
                ids.add(Long.valueOf(ma.group()));
            }
        }
        return ids;
    }

    /**
     * 清除Cq码
     *
     * @return 清除之后的文本
     */

    public String reovmCq() {
        return CQ.replaceAll("\\[(.*?)\\]", "");
    }

}
