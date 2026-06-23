package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import io.github.kingprimes.SwitchableDrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片插件控制器
 * <p>
 * 通过 {@link SwitchableDrawImagePlugin#switchTo(DrawImagePlugin)} 实现运行时热切换，
 * 无需 destroy/register Spring Bean，所有注入点自动感知新插件。
 * </p>
 *
 * @author KingPrimes
 */
@Slf4j
@RestController
@RequestMapping("/config/plugin/image")
public class DrawImagePluginController {

    private final DrawImagePluginManager manager;

    private final SwitchableDrawImagePlugin activePlugin;

    private final PluginConfigRepository repository;

    public DrawImagePluginController(DrawImagePluginManager manager,
                                     SwitchableDrawImagePlugin activePlugin,
                                     PluginConfigRepository repository) {
        this.manager = manager;
        this.activePlugin = activePlugin;
        this.repository = repository;
    }

    /**
     * 获取所有已加载的插件名称列表
     */
    @GetMapping("/list")
    public ApiResponse<Object> getPluginNames() {
        List<String> pluginNames = manager.getAllPlugins().stream()
                .map(DrawImagePlugin::getPluginName)
                .collect(Collectors.toList());
        return ApiResponse.ok("获取插件列表成功", pluginNames);
    }

    /**
     * 根据插件名称加载指定插件（运行时热切换）
     */
    @PostMapping("/load")
    public ApiResponse<Void> loadPluginByName(@RequestParam String pluginName) {
        DrawImagePlugin plugin = manager.getPluginByName(pluginName);
        if (plugin == null) {
            return ApiResponse.error(500, "未找到插件: " + pluginName);
        }
        // 原子切换代理，所有业务 Bean 通过 volatile 引用自动感知
        activePlugin.switchTo(plugin);
        savePluginSelection(pluginName);
        log.info("插件已切换到: {} v{}", plugin.getPluginName(), plugin.getPluginVersion());
        return ApiResponse.ok("插件加载成功: " + pluginName, null);
    }

    /**
     * 获取当前使用的插件名称
     */
    @GetMapping("/current")
    public ApiResponse<Object> getCurrentPlugin() {
        DrawImagePlugin current = activePlugin.getCurrent();
        return ApiResponse.ok("获取当前插件成功", current.getPluginName());
    }

    /**
     * 重新加载所有插件（扫描 plugin 目录）
     */
    @PostMapping("/reload")
    public ApiResponse<Void> reloadPlugins() {
        try {
            manager.loadPlugins("./plugin");

            // 保留当前已选插件名，重新匹配
            String currentName = activePlugin.getPluginName();
            DrawImagePlugin plugin = manager.getPluginByName(currentName);
            if (plugin == null) {
                plugin = manager.getFirstPlugin();
                log.warn("当前插件 {} 已不存在，回退到: {}", currentName, plugin.getPluginName());
            }
            activePlugin.switchTo(plugin);
            log.info("插件重新加载完成，共 {} 个插件", manager.getPluginCount());
            return ApiResponse.ok("插件重新加载完成，当前: " + plugin.getPluginName(), null);
        } catch (Exception e) {
            log.error("插件重新加载失败", e);
            return ApiResponse.error(500, "插件重新加载失败: " + e.getMessage());
        }
    }

    /**
     * 保存插件选择到数据库 — upsert 语义
     * <p>
     * 当前使用 {@code findAll().getFirst()} 取唯一配置记录，存在则更新，不存在则插入。
     * 始终维持表中仅一条记录作为"当前选中的插件"。
     */
    private void savePluginSelection(String pluginName) {
        List<PluginConfig> all = repository.findAll();
        if (all.isEmpty()) {
            PluginConfig config = new PluginConfig();
            config.setPluginName(pluginName);
            repository.save(config);
        } else {
            PluginConfig config = all.getFirst();
            config.setPluginName(pluginName);
            repository.save(config);
        }
    }
}
