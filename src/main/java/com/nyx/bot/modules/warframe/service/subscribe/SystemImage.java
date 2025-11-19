package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class SystemImage {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @Resource
    WorldStateUtils utils;


    /**
     * 添加系统图片
     *
     * @param builder 消息构建器
     * @param enums   类型
     */
    public void addSystemImage(ArrayMsgUtils builder, SubscribeEnums enums) throws DataNotInfoException {
        byte[] bytes = null;
        switch (enums) {
            case ALERTS -> bytes = drawImagePlugin.drawAlertsImage(utils.getAlerts());
            case VOID -> bytes = drawImagePlugin.drawVoidTraderImage(utils.getVoidTraders());
        }
        builder.img(bytes);
    }
}
