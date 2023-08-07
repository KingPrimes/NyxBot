package com.nyx.bot.custom;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.CoreEvent;
import com.nyx.bot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Primary
@Component
public class BotCoreEvent extends CoreEvent {

    @Override
    public void online(Bot bot) {
        log.info("已建立链接，BotId：{} \t 上线时间：{}",bot.getSelfId(), DateUtils.format(new Date(), DateUtils.yyyy));
    }

    @Override
    public void offline(long account) {
        log.info("机器人 {} 已断开链接",account);
    }


}
