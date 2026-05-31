package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 警报消息构建器
 * 构建警报任务的通知消息（使用图片）
 */
@Slf4j
@Component
public class AlertsMessageBuilder implements MessageBuilder<Alert> {

    private final DrawImagePlugin drawImagePlugin;

    private final WorldStateUtils worldStateUtils;

    public AlertsMessageBuilder(DrawImagePlugin drawImagePlugin, WorldStateUtils worldStateUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.worldStateUtils = worldStateUtils;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Alert> event, MissionSubscribeUserCheckType rule) {
        Alert alert = worldStateUtils.translateAlerts(event.data());

        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        try {
            // 尝试生成警报图片（传入单个警报的列表）
            byte[] image = drawImagePlugin.drawAlertsImage(Collections.singletonList(alert));
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成警报图片失败，使用文本消息: {}", e.getMessage());
            // 降级为文本消息
            builder.text("\n━━━━━ 新警报任务 ━━━━━");
            // 任务信息
            if (alert.getMissionInfo() != null) {
                var missionInfo = alert.getMissionInfo();

                // 节点
                if (missionInfo.getLocation() != null) {
                    builder.text("\n📍 节点: " + missionInfo.getLocation());
                }

                // 任务类型
                if (missionInfo.getMissionType() != null) {
                    builder.text("\n🎯 类型: " + missionInfo.getMissionType().getName());
                }

                // 敌人等级
                if (missionInfo.getMinEnemyLevel() != null && missionInfo.getMaxEnemyLevel() != null) {
                    builder.text(String.format("\n⚔️ 敌人等级: %d - %d",
                            missionInfo.getMinEnemyLevel(),
                            missionInfo.getMaxEnemyLevel()));
                }

                // 奖励
                if (missionInfo.getMissionReward() != null) {
                    var reward = missionInfo.getMissionReward();

                    // 现金奖励
                    if (reward.getCredits() != null && reward.getCredits() > 0) {
                        builder.text("\n💰 现金: " + reward.getCredits());
                    }

                    // 物品奖励
                    if (reward.getItems() != null && !reward.getItems().isEmpty()) {
                        builder.text("\n🎁 物品: " + String.join(", ", reward.getItems()));
                    }
                }
            }

            // 剩余时间
            if (alert.getTimeLeft() != null && !alert.getTimeLeft().isEmpty()) {
                builder.text("\n⏰ 剩余: " + alert.getTimeLeft());
            }

            builder.text("\n━━━━━━━━━━━━━━━━");
        }

        return builder;
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.ALERTS;
    }
}