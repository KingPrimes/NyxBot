package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.VoidTrader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 虚空商人消息构建器
 * <p>
 * 负责构建虚空商人Baro Ki'Teer的通知消息
 * </p>
 */
@Slf4j
@Component
public class VoidMessageBuilder implements MessageBuilder {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent event, MissionSubscribeUserCheckType rule) {
        VoidTrader trader = (VoidTrader) event.getData();
        
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        
        // 消息标题
        builder.text("\n━━━━━ 虚空商人 Baro ━━━━━");
        
        // 商人名称
        if (trader.getCharacter() != null && !trader.getCharacter().isEmpty()) {
            builder.text("\n👤 商人: " + trader.getCharacter());
        }
        
        // 位置信息
        if (trader.getNode() != null && !trader.getNode().isEmpty()) {
            builder.text("\n📍 位置: " + trader.getNode());
        }
        
        // 开始时间
        if (trader.getActivation() != null) {
            builder.text("\n🕐 开始: " + trader.getActivation());
        }
        
        // 过期时间
        if (trader.getExpiry() != null) {
            builder.text("\n⏰ 过期: " + trader.getExpiry());
        }
        
        builder.text("\n━━━━━━━━━━━━━━━━");
        
        log.debug("构建虚空商人消息完成 [位置:{}]", trader.getNode());
        return builder;
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.VOID;
    }
}