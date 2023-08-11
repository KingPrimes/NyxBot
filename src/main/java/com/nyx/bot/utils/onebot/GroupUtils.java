package com.nyx.bot.utils.onebot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;

import java.util.Objects;

public class GroupUtils {
    /**
     * 获取群头像Url地址
     *
     * @param groupId 群号
     * @return Url地址
     */
    public static String getGroupHeadImage(Long groupId) {
        //返回拼接的群头像网址
        return "https://p.qlogo.cn/gh/" + groupId + "/" + groupId + "/100";
    }


}
