package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.LiteSorite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArchonHuntMessageBuilder implements MessageBuilder<LiteSorite> {

    private final DrawImagePlugin drawImagePlugin;

    public ArchonHuntMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<LiteSorite> event, MissionSubscribeUserCheckType rule) {
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        try {
            byte[] image = drawImagePlugin.drawLiteSoriteImage(event.data());
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成执政官突击图片失败: {}", e.getMessage());
            LiteSorite hunt = event.data();
            builder.text("\n━━━━━ 执政官突击更新 ━━━━━")
                    .text("\nBoss: " + hunt.getBoss())
                    .text("\n奖励: " + hunt.getReward());
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.ARCHON_HUNT;
    }
}
