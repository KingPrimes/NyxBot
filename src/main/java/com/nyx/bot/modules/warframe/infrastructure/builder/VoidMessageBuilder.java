package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.VoidTrader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 虚空商人消息构建器
 * <p>
 * 负责构建虚空商人Baro Ki'Teer的通知消息
 * </p>
 */
@Slf4j
@Component
public class VoidMessageBuilder implements MessageBuilder<VoidTrader> {

    private final DrawImagePlugin drawImagePlugin;


    public VoidMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<VoidTrader> event, MissionSubscribeUserCheckType rule) {
        VoidTrader trader = event.data();
        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        if (trader.getManifest() != null && trader.getManifest().isEmpty()) {
            try {
                byte[] bytes = drawImagePlugin.drawVoidTraderImage(Collections.singletonList(trader));
                builder.img(bytes);
                return builder;
            } catch (Exception e) {
                return buildMessage(trader);
            }
        } else {
            return buildMessage(trader);
        }
    }

    private ArrayMsgUtils buildMessage(VoidTrader trader) {
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
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
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.VOID;
    }
}