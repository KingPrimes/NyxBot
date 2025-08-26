package com.nyx.bot.utils.onebot;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.http.HttpUtils;

import java.util.Date;

public class ImageUrlUtils {

    private final StringBuilder builder = new StringBuilder();


    private ImageUrlUtils(String url, Long botId, Long user, Long group, String rawMsg) {
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

    private ImageUrlUtils(String url, Bot bot, AnyMessageEvent event, Boolean isGet) {
        if (isGet == null) {
            isGet = true;
        }
        if (isGet) {
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
        } else {
            builder
                    .append(Constants.LOCALHOST)
                    .append("api/")
                    .append(url);
        }
    }

    private ImageUrlUtils(String url) {
        builder
                .append(Constants.LOCALHOST)
                .append("api/")
                .append(url);
    }

    public static String builder(String url, Long botId, Long user, Long group, String rawMsg) {
        return new ImageUrlUtils(url, botId, user, group, rawMsg).build();
    }

    public static String builder(String url, Bot bot, AnyMessageEvent event) {
        return new ImageUrlUtils(url, bot, event, true).build();
    }

    public static HttpUtils.Body builderBase64Post(String url, Bot bot, AnyMessageEvent event) {
        String build = new ImageUrlUtils(url, bot, event, false).build();
        OneBotLogInfoData oneBotLogInfoData = new OneBotLogInfoData();
        oneBotLogInfoData.setBotUid(bot.getSelfId());
        oneBotLogInfoData.setUserUid(event.getUserId());
        oneBotLogInfoData.setGroupUid(event.getGroupId());
        oneBotLogInfoData.setRawMsg(event.getRawMessage());
        oneBotLogInfoData.setTime(DateUtils.getDate());
        oneBotLogInfoData.setPermissionsEnums(Permissions.checkAdmin(bot, event));
        HttpUtils.Body body = HttpUtils.sendPostForFile(build, oneBotLogInfoData.toString());
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event, "图片上传失败，请检查网络连接", false);
        }
        return body;
    }

    public static HttpUtils.Body builderBase64Post(String url, OneBotLogInfoData data) {
        String build = new ImageUrlUtils(url).build();
        return HttpUtils.sendPostForFile(build, data.toString());
    }

    private String build() {
        return builder.toString();
    }


}
