package com.nyx.bot.modules.warframe.service.subscribe;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;

public class SystemImage {
    /**
     * 根据订阅类型返回图片Url后缀地址
     *
     * @param enums 订阅类型
     * @return 图片Url后缀地址
     */
    private static String gestural(SubscribeEnums enums) {
        switch (enums) {
            case ALERTS -> {
                return "postSubAlertsImage";
            }
            case ARBITRATION -> {
                return "getArbitrationImage";
            }
            case DAILY_DEALS -> {
                return "postDailyDealsImage";
            }
            case VOID -> {
                return "postVoidImage";
            }
            case CETUS_CYCLE -> {
                return "postAllCycleImage";
            }
            case INVASIONS -> {
                return "postSubInvasionsImage";
            }
            case STEEL_PATH -> {
                return "postSteelPathImage";
            }
            case NIGHTWAVE -> {
                return "postNighTwaveImage";
            }
            case SORTIE -> {
                return "postAssaultImage";
            }
            case ARCHON_HUNT -> {
                return "postArsonHuntImage";
            }
            case DUVIRI_CYCLE -> {
                return "postDuviriCycleImage";
            }
            case FISSURES -> {
                return "postSubscribeFissuresImage";
            }
            default -> {
                return "";
            }
        }
    }

    /**
     * 添加系统图片
     *
     * @param builder   消息构建器
     * @param enums     类型
     * @param subscribe 订阅组
     * @param user      用户
     * @param o         数据
     */
    public static void addSystemImage(ArrayMsgUtils builder, SubscribeEnums enums, MissionSubscribe subscribe, MissionSubscribeUser user, Object o) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                gestural(enums), new OneBotLogInfoData(
                        subscribe.getSubBotUid(),
                        user.getUserId(),
                        subscribe.getSubGroup(),
                        enums.getNAME(),
                        DateUtils.getDate(),
                        PermissionsEnums.MANAGE,
                        Codes.WARFRAME_SUBSCRIBE,
                        o != null ? JSON.toJSONString(o) : "")
        );
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            builder.img(body.getFile());
        }
    }
}
