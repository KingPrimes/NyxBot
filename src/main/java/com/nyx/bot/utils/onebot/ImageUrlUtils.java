package com.nyx.bot.utils.onebot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.core.Constants;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.HttpUtils;

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

    public ImageUrlUtils(String url,Bot bot, AnyMessageEvent event,Boolean isGetOrPost){
        if(isGetOrPost == null){
            isGetOrPost = true;
        }
        if(isGetOrPost){
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
        }else{
            builder
                    .append(Constants.LOCALHOST)
                    .append("api/")
                    .append(url);
        }
    }

    public ImageUrlUtils(){

    }

    public static String builder(String url,Long botId,Long user,Long group,String rawMsg) {
        return new ImageUrlUtils(url, botId, user, group, rawMsg).build();
    }

    public static String builder(String url,Bot bot,AnyMessageEvent event) {
        return new ImageUrlUtils(url, bot,event,true).build();
    }

    public static byte[] builderBase64(String url,Bot bot,AnyMessageEvent event){
        String build = new ImageUrlUtils(url, bot, event,true).build();
        return HttpUtils.sendGetForFile(build);
    }

    public static byte[] builderBase64Post(String url,Bot bot,AnyMessageEvent event){
        String build = new ImageUrlUtils(url, bot, event,false).build();
        OneBotLogInfoData oneBotLogInfoData = new OneBotLogInfoData();
        oneBotLogInfoData.setBotUid(bot.getSelfId());
        oneBotLogInfoData.setUserUid(event.getUserId());
        oneBotLogInfoData.setGroupUid(event.getGroupId());
        oneBotLogInfoData.setRawMsg(event.getRawMessage());
        oneBotLogInfoData.setTime(DateUtils.getDate());
        oneBotLogInfoData.setPermissionsEnums(Permissions.checkAdmin(bot, event));
        return HttpUtils.sendPostForFile(build, oneBotLogInfoData.toString());
    }

    private String build(){
        return builder.toString();
    }


}
