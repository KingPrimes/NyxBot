package com.nyx.bot.modules.warframe.domain.service;

import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;

import java.util.List;

/**
 * 变化检测器接口
 * 用于检测游戏状态的变化
 * <p>
 * 实现类应使用 @Component 注解，Spring会自动注入到应用服务中
 */
public interface ChangeDetector {

    /**
     * 检测游戏状态变化
     *
     * @param oldState 旧状态
     * @param newState 新状态
     * @return 变化事件列表，无变化返回空列表
     */
    List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState);

    /**
     * 获取支持的订阅类型
     *
     * @return 订阅类型
     */
    SubscribeEnums getSupportedType();
}