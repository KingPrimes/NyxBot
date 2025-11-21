package com.nyx.bot.common.config;

import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 图片插件管理器
 */
@Configuration
public class DrawImagePluginManagerConfig {

    @Resource
    PluginConfigRepository repository;

    /**
     * 创建图片插件管理器
     */
    @Bean
    public DrawImagePluginManager drawImagePluginManager() {
        DrawImagePluginManager manager = new DrawImagePluginManager();
        // 加载插件
        manager.loadPlugins("./plugin");
        return manager;
    }

    /**
     * 获取图片插件</br>
     * 默认获取第一个插件</br>
     * 当插件目录不存在插件时获取的是默认插件实现</br>
     */
    @Bean
    public DrawImagePlugin drawImagePlugin() {
        DrawImagePluginManager manager = drawImagePluginManager();
        List<PluginConfig> all = repository.findAll();
        if (!all.isEmpty()) {
            String pluginName = all.getFirst().getPluginName();
            if (pluginName != null && !pluginName.isEmpty()) {
                DrawImagePlugin plugin = manager.getPluginByName(pluginName);
                if (plugin != null) {
                    return plugin;
                }
            }
        }
        return drawImagePluginManager().getFirstPlugin();
    }

    /**
     * 动态注册插件bean
     * @param plugin 要注册的插件
     */
    public static void registerDrawImagePlugin(@NonNull DrawImagePlugin plugin) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtils.getApplicationContext().getAutowireCapableBeanFactory();
        beanFactory.destroySingleton("drawImagePlugin");
        beanFactory.registerSingleton("drawImagePlugin", plugin);
    }
}
