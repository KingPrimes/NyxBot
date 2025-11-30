package com.nyx.bot.modules.warframe.domain.service;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.SubscribeEnums;

/**
 * 消息构建器接口
 * 用于根据变化事件构建通知消息
 * <p>
 * 实现类应使用 @Component 注解，Spring会自动注入到应用服务中
 */
public interface MessageBuilder<T> {

    /**
     * 构建消息内容
     *
     * @param event 变化事件
     * @param rule  用户订阅规则（用于个性化消息）
     * @return 消息构建器
     */
    ArrayMsgUtils buildMessage(ChangeEvent<T> event, MissionSubscribeUserCheckType rule);

    /**
     * 获取支持的订阅类型
     *
     * @return 订阅类型
     */
    SubscribeEnums getSupportedType();
}