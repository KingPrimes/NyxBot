package com.nyx.bot.custom;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.CoreEvent;
import com.nyx.bot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Primary
@Component
public class BotCoreEvent extends CoreEvent {
    WebSocketSession session;

    @Override
    public void online(Bot bot) {
        log.info("已建立链接，BotId：{} \t 上线时间：{}", bot.getSelfId(), DateUtils.format(new Date(), DateUtils.YYYY));
    }

    @Override
    public void offline(long account) {
        try {
            session.close();
        } catch (IOException ignored) {
        }
        log.info("机器人 {} 已断开链接", account);
    }

    @Override
    public boolean session(WebSocketSession session) {
        this.session = session;
        log.debug("---------------------------------------------------");
        log.debug("Attributes:{}", session.getAttributes());
        log.debug("Headers:{}", session.getHandshakeHeaders());
        log.debug("AcceptedProtocol:{}", session.getAcceptedProtocol());
        log.debug("LocalAddress:{}", session.getLocalAddress());
        log.debug("Extensions:{}", session.getExtensions());
        log.debug("---------------------------------------------------");
        return super.session(session);
    }
}
