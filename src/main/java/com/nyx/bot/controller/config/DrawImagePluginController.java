package com.nyx.bot.controller.config;

import com.nyx.bot.common.config.DrawImagePluginManagerConfig;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.entity.PluginConfig;
import com.nyx.bot.repo.PluginConfigRepository;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import jakarta.annotation.Resource;
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

    @Resource
    DrawImagePluginManager drawImagePluginManager;

    @Resource
    PluginConfigRepository repository;

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
    public AjaxResult getPluginNames() {
        List<String> pluginNames = drawImagePluginManager.getAllPlugins().stream()
                .map(DrawImagePlugin::getPluginName)
                .collect(Collectors.toList());
        return AjaxResult.success("获取插件列表成功", pluginNames);
    }

    /**
     * 根据插件名称加载指定插件
     *
     * @param pluginName 插件名称
     * @return 加载结果信息
     */
    @PostMapping("/load")
    public AjaxResult loadPluginByName(@RequestParam String pluginName) {
        DrawImagePlugin plugin = drawImagePluginManager.getPluginByName(pluginName);
        if (plugin != null) {
            DrawImagePluginManagerConfig.registerDrawImagePlugin(plugin);
            savePluginSelection(pluginName);
            return AjaxResult.success("插件加载成功: " + pluginName);
        } else {
            return AjaxResult.error("未找到插件: " + pluginName);
        }
    }

    /**
     * 获取当前使用的插件名称
     *
     * @return 当前插件名称
     */
    @GetMapping("/current")
    public AjaxResult getCurrentPlugin() {
        DrawImagePlugin currentPlugin = SpringUtils.getBean(DrawImagePlugin.class);
        return AjaxResult.success("获取当前插件成功", currentPlugin.getPluginName());
    }

    /**
     * 重新加载所有插件
     *
     * @return 重新加载结果
     */
    @PostMapping("/reload")
    public AjaxResult reloadPlugins() {
        try {
            drawImagePluginManager.loadPlugins("./plugin");
            DrawImagePlugin firstPlugin = drawImagePluginManager.getFirstPlugin();
            DrawImagePluginManagerConfig.registerDrawImagePlugin(firstPlugin);
            log.info("插件重新加载完成，目录: {}", "./plugin");
            return AjaxResult.success("插件重新加载完成，共加载了 " + drawImagePluginManager.getPluginCount() + " 个插件");
        } catch (Exception e) {
            log.error("插件重新加载失败", e);
            return AjaxResult.error("插件重新加载失败: " + e.getMessage());
        }
    }
}