package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.DuvalierCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DuviriCycleMessageBuilder implements MessageBuilder<DuvalierCycle> {

    private final DrawImagePlugin drawImagePlugin;

    public DuviriCycleMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<DuvalierCycle> event, MissionSubscribeUserCheckType rule) {
        DuvalierCycle cycle = event.data();
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        try {
            byte[] image = drawImagePlugin.drawDuviriCycleImage(cycle);
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成双衍王境图片失败: {}", e.getMessage());
            builder.text("\n双衍王境轮换已更新，请查看游戏内详情");
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.DUVIRI_CYCLE;
    }
}
