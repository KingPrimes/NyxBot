package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.SeasonInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NightwaveMessageBuilder implements MessageBuilder<SeasonInfo> {

    private final DrawImagePlugin drawImagePlugin;

    public NightwaveMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<SeasonInfo> event, MissionSubscribeUserCheckType rule) {
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        try {
            byte[] image = drawImagePlugin.drawSeasonInfoImage(event.data());
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成电波图片失败: {}", e.getMessage());
            SeasonInfo info = event.data();
            builder.text("\n━━━━━ 电波更新 ━━━━━")
                    .text("\n赛季: " + info.getSeason());
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.NIGHTWAVE;
    }
}
