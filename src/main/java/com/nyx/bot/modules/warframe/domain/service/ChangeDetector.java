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
public interface ChangeDetector<T> {

    /**
     * 检测游戏状态变化
     *
     * @param oldState 旧状态
     * @param newState 新状态
     * @return 变化事件列表，无变化返回空列表
     */
    List<ChangeEvent<T>> detectChanges(WorldState oldState, WorldState newState);

    /**
     * 获取支持的订阅类型
     *
     * @return 订阅类型
     */
    SubscribeEnums getSupportedType();

    /**
     * 清理当前订阅类型的过期历史记录
     * <p>
     * 默认实现为无操作，子类可根据需要重写此方法
     * <p>
     * 清理规则：删除 notifiedAt 早于 (当前时间 - 保留时长) 的记录
     * <p>
     * 注意：
     * <ul>
     *   <li>此方法会在 {@link #detectChanges(WorldState, WorldState)} 执行前被调用</li>
     *   <li>清理失败不应中断检测流程</li>
     *   <li>实现类应妥善处理异常，避免影响主流程</li>
     * </ul>
     */
    default void cleanExpiredHistory() {
        // 默认无操作
    }
}