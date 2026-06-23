package com.nyx.bot.common.config;

import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import io.github.kingprimes.JnaNativePluginLoader;
import io.github.kingprimes.SwitchableDrawImagePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Configuration
public class DrawImagePluginManagerConfig {

    private static final Logger log = LoggerFactory.getLogger(DrawImagePluginManagerConfig.class);

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
     * <p>
     * 优先使用数据库记录中保存的插件名匹配；未找到时回退到 {@code manager.getFirstPlugin()}。
     * 如果插件目录为空，getFirstPlugin() 返回内置的 DefaultDrawImagePlugin 兜底。
     * </p>
     */
    private DrawImagePlugin resolveInitialPlugin(DrawImagePluginManager manager) {
        // 1. 从数据库读取已选插件
        List<PluginConfig> all = repository.findAll();
        if (!all.isEmpty()) {
            String pluginName = all.getFirst().getPluginName();
            if (pluginName != null && !pluginName.isEmpty()) {
                DrawImagePlugin plugin = manager.getPluginByName(pluginName);
                if (plugin != null) {
                    log.info("数据库记录: 使用插件 {} v{}", pluginName, plugin.getPluginVersion());
                    return plugin;
                }
                log.warn("数据库记录的插件 {} 未找到（可能已被移除），回退到默认", pluginName);
            }
        }
        // 2. 回退到第一个可用插件
        int count = manager.getPluginCount();
        DrawImagePlugin fallback = manager.getFirstPlugin();
        log.info("使用默认插件: {} v{}（共 {} 个插件）",
                fallback.getPluginName(), fallback.getPluginVersion(), count);
        return fallback;
    }
}
