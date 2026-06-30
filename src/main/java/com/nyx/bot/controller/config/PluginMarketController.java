package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.pluginmarket.MarketException;
import com.nyx.bot.pluginmarket.PluginIndex;
import com.nyx.bot.pluginmarket.PluginMarketService;
import com.nyx.bot.pluginmarket.UpdateInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 插件市场控制器。
 * <p>
 * 提供 Web UI 调用的插件市场 API，所有操作为按需触发。
 * </p>
 *
 * @author KingPrimes
 */
@Slf4j
@RestController
@RequestMapping("/config/plugin/market")
public class PluginMarketController extends BaseController {

    private final PluginMarketService marketService;

    public PluginMarketController(PluginMarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * 拉取并返回市场插件列表。
     * <p>
     * 每次请求实时从 GitHub raw 拉取最新索引，支持按名称、类型、标签过滤。
     * </p>
     *
     * @param keyword 名称关键词（可选，模糊匹配 name 或 displayName）
     * @param type    插件类型（可选，精确匹配如 "jar"、"native"）
     * @param tags    标签过滤（可选，逗号分隔，如 "draw,native"）
     */
    @GetMapping("/list")
    public ApiResponse<?> listMarket(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tags) {
        try {
            PluginIndex index = marketService.searchPlugins(keyword, type, tags);
            return success("获取市场列表成功", index);
        } catch (MarketException e) {
            log.warn("获取市场列表失败: {}", e.getMessage());
            return error("获取市场列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查所有已安装插件是否有可用更新。
     * <p>
     * 比对本地已安装版本和市场最新版本。
     * </p>
     */
    @PostMapping("/check-update")
    public ApiResponse<?> checkUpdate() {
        try {
            PluginIndex index = marketService.fetchIndex();
            List<UpdateInfo> updates = marketService.checkUpdates(index);
            return success("检查更新完成", updates);
        } catch (MarketException e) {
            log.warn("检查更新失败: {}", e.getMessage());
            return error("检查更新失败: " + e.getMessage());
        }
    }

    /**
     * 安装或升级插件。
     * <p>
     * 从市场最新索引中查找指定插件和版本，下载后热加载。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     * @param version    要安装的版本号；传 {@code latest} 表示安装最新版
     */
    @PostMapping("/install")
    public ApiResponse<Void> install(@RequestParam String pluginName,
                                     @RequestParam(defaultValue = "latest") String version) {
        try {
            marketService.install(pluginName, version);
            log.info("插件安装成功: {} v{}", pluginName, version);
            return success("插件安装成功: " + pluginName);
        } catch (MarketException e) {
            log.warn("插件安装失败: {}", e.getMessage(), e);
            return error("插件安装失败: " + e.getMessage());
        }
    }

    /**
     * 卸载插件。
     * <p>
     * 删除 jar 文件、清理数据库记录、自动回退当前活跃插件。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     */
    @PostMapping("/uninstall")
    public ApiResponse<Void> uninstall(@RequestParam String pluginName) {
        try {
            marketService.uninstall(pluginName);
            log.info("插件卸载成功: {}", pluginName);
            return success("插件卸载成功: " + pluginName);
        } catch (MarketException e) {
            log.warn("插件卸载失败: {}", e.getMessage(), e);
            return error("插件卸载失败: " + e.getMessage());
        }
    }
}
