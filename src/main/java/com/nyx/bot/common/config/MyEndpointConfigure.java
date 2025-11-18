package com.nyx.bot.common.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * WebSocket端点配置类
 * 用于配置和管理WebSocket端点实例的创建
 */
public class MyEndpointConfigure extends ServerEndpointConfig.Configurator implements ApplicationContextAware {
    private static volatile BeanFactory context;

    /**
     * 获取端点实例
     *
     * @param clazz 端点类
     * @param <T>   端点类型
     * @return 端点实例
     */
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) {
        return context.getBean(clazz);
    }


    /**
     * 设置应用程序上下文
     *
     * @param applicationContext 应用程序上下文
     * @throws BeansException Bean异常
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        MyEndpointConfigure.context = applicationContext;
    }
}