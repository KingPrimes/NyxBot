package com.nyx.bot.utils.onebot;

public class GroupUtils {
    /**
     * 获取群头像Url地址
     *
     * @param groupId 群号
     * @return Url地址
     */
    public static String getGroupHeadImage(Long groupId) {
        //返回拼接的群头像网址
        return String.format("https://p.qlogo.cn/gh/%s/%s/100", groupId, groupId);
    }


}
