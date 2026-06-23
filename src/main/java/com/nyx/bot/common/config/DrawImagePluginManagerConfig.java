package com.nyx.bot.common.config;

import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import io.github.kingprimes.JnaNativePluginLoader;
import io.github.kingprimes.SwitchableDrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 图片插件管理器
 * <p>
 * 同时支持 jar 插件和 native（.dll/.so/.dylib）插件加载。
 * 提供 {@link SwitchableDrawImagePlugin} 热切换代理，运行时切换不中断业务 Bean。
 * </p>
 *
 * @author KingPrimes
 */
@Slf4j
@Configuration
public class DrawImagePluginManagerConfig {

    private final PluginConfigRepository repository;

    public DrawImagePluginManagerConfig(PluginConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建图片插件管理器，同时支持 jar 和 native 插件
     */
    @Bean
    public DrawImagePluginManager drawImagePluginManager() {
        DrawImagePluginManager manager = new DrawImagePluginManager(new JnaNativePluginLoader());
        // loadPlugins 涉及 URLClassLoader + ServiceLoader，不在虚拟线程上调用
        manager.loadPlugins("./plugin");
        log.info("DrawImagePlugin 加载完成，共 {} 个插件", manager.getPluginCount());
        return manager;
    }

    /**
     * 创建可切换的图片插件代理
     */
    @Bean
    public SwitchableDrawImagePlugin switchableDrawImagePlugin(DrawImagePluginManager manager) {
        DrawImagePlugin initial = resolveInitialPlugin(manager);
        log.info("初始图片插件: {} v{}", initial.getPluginName(), initial.getPluginVersion());
        return new SwitchableDrawImagePlugin(initial);
    }

    /**
     * 对外暴露 {@link DrawImagePlugin} 接口。
     * 实际返回 {@link SwitchableDrawImagePlugin} 代理实例，
     * 所有业务 Bean 通过此接口注入，运行时切换插件无需重新注入。
     */
    @Bean
    public DrawImagePlugin drawImagePlugin(SwitchableDrawImagePlugin sp) {
        return sp;
    }

    /**
     * 从数据库解析初始插件
     */
    private DrawImagePlugin resolveInitialPlugin(DrawImagePluginManager manager) {
        List<PluginConfig> all = repository.findAll();
        if (!all.isEmpty()) {
            String pluginName = all.getFirst().getPluginName();
            if (pluginName != null && !pluginName.isEmpty()) {
                DrawImagePlugin plugin = manager.getPluginByName(pluginName);
                if (plugin != null) {
                    log.info("数据库记录: 使用插件 {}", pluginName);
                    return plugin;
                }
                log.warn("数据库记录的插件 {} 未找到，回退到默认", pluginName);
            }
        }
        return manager.getFirstPlugin();
    }
}
