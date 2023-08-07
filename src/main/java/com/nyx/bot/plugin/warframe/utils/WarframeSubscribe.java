package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.SpringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class WarframeSubscribe {

    public static void isUpdated(SocketGlobalStates states) {


        Cache cache = SpringUtils.getBean(CacheManager.class).getCache("warframe-socket-data");
        SocketGlobalStates d = cache.get("data", SocketGlobalStates.class);
        // 检查缓存内容是否为空
        Optional.ofNullable(d).ifPresentOrElse(data->{

            GlobalStates form = states.getPacket().getData();
            GlobalStates cacheData = data.getPacket().getData();

            //检查仲裁信息是否为空值
            Optional.ofNullable(form.getArbitration()).ifPresentOrElse(f->{

            },()->{
                //为空则使用缓存中的仲裁信息覆盖
                states.getPacket().getData().setArbitration(cacheData.getArbitration());
            });

            //检查警报是否不为空
            Optional.ofNullable(form.getAlerts()).ifPresent(f->{
                //判断数据是否有更新
                if (f.equals(cacheData.getAlerts())) {
                    WarframeDataUpdateMission.updateAlerts();
                }
            });

            //检查 夜灵平野 是否为空
            Optional.ofNullable(form.getCetusCycle()).ifPresent(f->{
                //判断是否更新
                if (form.getCetusCycle().getState().toLowerCase(Locale.ROOT).equals("day")) {
                    if (DateUtils.getDateHour(form.getCetusCycle().getExpiry(), new Date()) == 0) {
                        long date = DateUtils.getDateMin(form.getCetusCycle().getExpiry(), new Date());
                        if (date == 13) {
                            WarframeDataUpdateMission.updateCetusCycle(DateUtils.getDiff(form.getCetusCycle().getExpiry(), new Date()));
                        }
                    }
                }
            });




            cache.put("data",states);
        },()->{
            cache.put("data",states);
        });
    }
}
