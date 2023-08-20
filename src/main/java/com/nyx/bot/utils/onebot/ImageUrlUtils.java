package com.nyx.bot.utils.onebot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.core.Constants;

import java.util.Date;

public class ImageUrlUtils {

    private final StringBuilder builder = new StringBuilder();


    public ImageUrlUtils(String url,Long botId,Long user,Long group,String rawMsg) {
        builder
                .append(Constants.LOCALHOST)
                .append("api/")
                .append(url)
                .append("/")
                .append(botId)
                .append("/")
                .append(user)
                .append("/")
                .append(group)
                .append("/")
                .append(rawMsg)
                .append("/")
                .append(new Date().getTime())
        ;
    }

    public ImageUrlUtils(String url,Bot bot, AnyMessageEvent event){
        builder
                .append(Constants.LOCALHOST)
                .append("api/")
                .append(url)
                .append("/")
                .append(bot.getSelfId())
                .append("/")
                .append(event.getUserId())
                .append("/")
                .append(event.getGroupId())
                .append("/")
                .append(event.getRawMessage())
                .append("/")
                .append(new Date().getTime())
        ;
    }

    public static String builder(String url,Long botId,Long user,Long group,String rawMsg) {
        return new ImageUrlUtils(url, botId, user, group, rawMsg).build();
    }

    public static String builder(String url,Bot bot,AnyMessageEvent event) {
        return new ImageUrlUtils(url, bot,event).build();
    }

    private String build(){
        return builder.toString();
    }


}
