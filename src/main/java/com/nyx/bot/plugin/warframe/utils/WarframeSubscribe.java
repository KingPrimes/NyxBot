package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public class WarframeSubscribe {
    public static void isUpdated(SocketGlobalStates states) {
        isUpdated(states.getPacket().getData());
    }

    public static void isUpdated(GlobalStates states) {
        //log.info("取得数据：{}", states.toString());
        try {
            //获取缓存数据
            GlobalStates d = CacheUtils.getGlobalState();
            Optional.of(d).ifPresentOrElse(data -> {

                //检查仲裁信息是否为空值
                Optional.ofNullable(states.getArbitration()).ifPresentOrElse(f -> {
                    if (!f.equals(data.getArbitration())) {
                        WarframeDataUpdateMission.updateArbitration();
                    }
                }, () -> {
                    //为空则使用缓存中的仲裁信息覆盖
                    states.setArbitration(data.getArbitration());
                });

                //检查警报是否不为空
                Optional.ofNullable(states.getAlerts()).ifPresent(f -> {
                    //判断数据是否有更新
                    if (!f.equals(data.getAlerts())) {
                        WarframeDataUpdateMission.updateAlerts();
                    }
                });

                //检查 夜灵平野 是否为空
                Optional.ofNullable(states.getCetusCycle()).ifPresent(f -> {
                    //判断是否更新
                    if (states.getCetusCycle().getState().toLowerCase(Locale.ROOT).equals("day")) {
                        //判断相差的小时是否在一小时以内
                        if (DateUtils.getDateHour(states.getCetusCycle().getExpiry(), new Date()) == 0) {
                            //取相差的分钟
                            long date = DateUtils.getDateMin(states.getCetusCycle().getExpiry(), new Date());
                            //相差13分钟的时候发送提醒
                            if (date == 13) {
                                //发送提醒
                                WarframeDataUpdateMission.updateCetusCycle(DateUtils.getDiff(states.getCetusCycle().getExpiry(), new Date()));
                            }
                        }
                    }
                });

                //更新数据
                CacheUtils.setGlobalState(states);
            }, () -> {
                //更新数据
                CacheUtils.setGlobalState(states);
            });
        } catch (Exception e) {
            //设置数据
            CacheUtils.setGlobalState(states);
        }

    }
}
