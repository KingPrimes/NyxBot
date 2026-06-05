package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.Sortie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SortieMessageBuilder implements MessageBuilder<Sortie> {

    private final DrawImagePlugin drawImagePlugin;

    public SortieMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Sortie> event, MissionSubscribeUserCheckType rule) {
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        try {
            byte[] image = drawImagePlugin.drawSortiesImage(event.data());
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成突击图片失败: {}", e.getMessage());
            Sortie sortie = event.data();
            builder.text("\n━━━━━ 突击更新 ━━━━━")
                    .text("\nBoss: " + sortie.getBoss())
                    .text("\n奖励: " + sortie.getReward());
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.SORTIE;
    }
}
