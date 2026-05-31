package com.nyx.bot.controller.config;

import com.nyx.bot.common.config.DrawImagePluginManagerConfig;
import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片插件控制器
 * 用于管理图片插件的加载和选择
 */
@Slf4j
@RestController
@RequestMapping("/config/plugin/image")
public class DrawImagePluginController {

    private final DrawImagePluginManager drawImagePluginManager;

    private final PluginConfigRepository repository;

    public DrawImagePluginController(DrawImagePluginManager drawImagePluginManager, PluginConfigRepository repository) {
        this.drawImagePluginManager = drawImagePluginManager;
        this.repository = repository;
    }

    /**
     * 保存插件选择到数据库
     *
     * @param pluginName 插件名称
     */
    private void savePluginSelection(String pluginName) {
        repository.findByPluginName(pluginName).ifPresentOrElse(
                config -> {
                },
                () -> {
                    PluginConfig newConfig = new PluginConfig();
                    newConfig.setId(1L);
                    newConfig.setPluginName(pluginName);
                    repository.save(newConfig);
                }
        );
    }

    /**
     * 获取所有已加载的插件名称列表
     *
     * @return 插件名称列表
     */
    @GetMapping("/list")
    public ApiResponse<Object> getPluginNames() {
        List<String> pluginNames = drawImagePluginManager.getAllPlugins().stream()
                .map(DrawImagePlugin::getPluginName)
                .collect(Collectors.toList());
        return ApiResponse.ok("获取插件列表成功", pluginNames);
    }

    /**
     * 根据插件名称加载指定插件
     *
     * @param pluginName 插件名称
     * @return 加载结果信息
     */
    @PostMapping("/load")
    public ApiResponse<Void> loadPluginByName(@RequestParam String pluginName) {
        DrawImagePlugin plugin = drawImagePluginManager.getPluginByName(pluginName);
        if (plugin != null) {
            DrawImagePluginManagerConfig.registerDrawImagePlugin(plugin);
            savePluginSelection(pluginName);
            return ApiResponse.ok("插件加载成功: " + pluginName, null);
        } else {
            return ApiResponse.error(500, "未找到插件: " + pluginName);
        }
    }

    /**
     * 获取当前使用的插件名称
     *
     * @return 当前插件名称
     */
    @GetMapping("/current")
    public ApiResponse<Object> getCurrentPlugin() {
        DrawImagePlugin currentPlugin = SpringUtils.getBean(DrawImagePlugin.class);
        return ApiResponse.ok("获取当前插件成功", currentPlugin.getPluginName());
    }

    /**
     * 重新加载所有插件
     *
     * @return 重新加载结果
     */
    @SuppressWarnings("null")
    @PostMapping("/reload")
    public ApiResponse<Void> reloadPlugins() {
        try {
            drawImagePluginManager.loadPlugins("./plugin");
            DrawImagePlugin firstPlugin = drawImagePluginManager.getFirstPlugin();
            DrawImagePluginManagerConfig.registerDrawImagePlugin(firstPlugin);
            log.info("插件重新加载完成，目录: {}", "./plugin");
            return ApiResponse.ok("插件重新加载完成，共加载了 " + drawImagePluginManager.getPluginCount() + " 个插件", null);
        } catch (Exception e) {
            log.error("插件重新加载失败", e);
            return ApiResponse.error(500, "插件重新加载失败: " + e.getMessage());
        }
    }
}
