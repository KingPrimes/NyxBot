package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.ActiveMission;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 裂隙消息构建器
 * 构建裂隙任务的通知消息
 */
@Slf4j
@Component
public class FissuresMessageBuilder implements MessageBuilder<ActiveMission> {

    @Resource
    private WorldStateUtils worldStateUtils;

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<ActiveMission> event, MissionSubscribeUserCheckType rule) {
        ActiveMission mission = worldStateUtils.translateActiveMission(event.getData());

        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        // 消息标题
        builder.text("\n━━━━━ 新裂隙任务 ━━━━━");

        // 节点信息
        if (mission.getNode() != null) {
            builder.text("\n📍 节点: " + mission.getNode());
        }

        // 任务类型
        if (mission.getMissionType() != null) {
            builder.text("\n🎯 类型: " + mission.getMissionType().getName());
        }

        // 遗物等级
        if (mission.getModifier() != null) {
            String tierName = mission.getModifier().getName();
            builder.text("\n⭐ 等级: " + tierName);
        }

        // 派系
        if (mission.getFaction() != null) {
            builder.text("\n⚔️ 派系: " + mission.getFaction().getName());
        }

        // 剩余时间
        if (mission.getTimeLeft() != null && !mission.getTimeLeft().isEmpty()) {
            builder.text("\n⏰ 剩余: " + mission.getTimeLeft());
        }

        // 是否为钢铁之路
        if (mission.getHard() != null && mission.getHard()) {
            builder.text("\n🔥 [钢铁之路]");
        }

        builder.text("\n━━━━━━━━━━━━━━━━");


        return builder;
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.FISSURES;
    }
}