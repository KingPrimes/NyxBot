package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.ActiveMission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 裂隙消息构建器
 * 构建裂隙任务的通知消息
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class FissuresMessageBuilder implements MessageBuilder {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent event, MissionSubscribeUserCheckType rule) {
        ActiveMission mission = (ActiveMission) event.getData();

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
            String tierName = getTierName(mission.getModifier().name());
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

    /**
     * 获取遗物等级名称
     */
    private String getTierName(String voidType) {
        return switch (voidType) {
            case "VoidT1" -> "古纪 (Lith)";
            case "VoidT2" -> "前纪 (Meso)";
            case "VoidT3" -> "中纪 (Neo)";
            case "VoidT4" -> "后纪 (Axi)";
            case "VoidT5" -> "安魂 (Requiem)";
            default -> voidType;
        };
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.FISSURES;
    }
}