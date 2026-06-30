package com.nyx.bot.pluginmarket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.config.ConfigConstants;
import com.nyx.bot.common.config.LocateYamlService;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.entity.PluginInfo;
import com.nyx.bot.repo.PluginInfoRepository;
import com.nyx.bot.utils.http.HttpFileDownloader;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import io.github.kingprimes.SwitchableDrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件市场服务。
 * <p>
 * 提供市场索引拉取、版本比对、安装/卸载等按需操作。
 * 所有操作由用户在 Web UI 点击触发，无定时任务。
 * </p>
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class PluginMarketService {

    private static final Path PLUGIN_DIR = Path.of("./plugin");

    private static final String LATEST_TAG = "latest";

    private final PluginInfoRepository pluginInfoRepository;

    private final LocateYamlService yamlService;

    private final DrawImagePluginManager manager;

    private final SwitchableDrawImagePlugin activePlugin;

    private final ObjectMapper objectMapper;

    public PluginMarketService(PluginInfoRepository pluginInfoRepository,
                               LocateYamlService yamlService,
                               DrawImagePluginManager manager,
                               SwitchableDrawImagePlugin activePlugin,
                               ObjectMapper objectMapper) {
        this.pluginInfoRepository = pluginInfoRepository;
        this.yamlService = yamlService;
        this.manager = manager;
        this.activePlugin = activePlugin;
        this.objectMapper = objectMapper;
    }

    // ══════════════════════════════════════════════
    // 市场查询
    // ══════════════════════════════════════════════

    /**
     * 从 GitHub raw 拉取插件市场索引并解析。
     * <p>
     * 由用户点击"刷新市场"触发，无缓存。
     * </p>
     *
     * @return 解析后的插件市场索引
     * @throws MarketException 网络失败或 JSON 解析失败时抛出
     */
    public PluginIndex fetchIndex() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX);
        if (!body.is2xxSuccessful()) {
            throw new MarketException("获取市场索引失败: HTTP " + body.code());
        }
        try {
            return objectMapper.readValue(body.body(), PluginIndex.class);
        } catch (IOException e) {
            log.error("解析市场索引 JSON 失败", e);
            throw new MarketException("解析市场索引数据失败: " + e.getMessage());
        }
    }

    /**
     * 拉取市场索引并按条件搜索过滤。
     * <p>
     * 支持按名称关键词、类型、标签过滤。所有过滤条件均可选，不传则返回全部。
     * </p>
     *
     * @param keyword 名称关键词（模糊匹配 name 或 displayName，大小写不敏感）
     * @param type    插件类型过滤（精确匹配，如 "jar"、"native"）
     * @param tags    标签过滤（逗号分隔，插件必须包含所有指定标签）
     * @return 过滤后的市场索引
     * @throws MarketException 网络失败或 JSON 解析失败时抛出
     */
    public PluginIndex searchPlugins(String keyword, String type, String tags) {
        PluginIndex index = fetchIndex();
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasType = type != null && !type.isBlank();
        boolean hasTags = tags != null && !tags.isBlank();

        if (!hasKeyword && !hasType && !hasTags) {
            return index;
        }

        List<String> tagList = hasTags
                ? List.of(tags.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList()
                : List.of();

        Map<String, PluginIndexEntry> filtered = index.getPlugins().entrySet().stream()
                .filter(e -> matches(e.getValue(), keyword, type, tagList))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        PluginIndex result = new PluginIndex();
        result.setSchemaVersion(index.getSchemaVersion());
        result.setMarketplace(index.getMarketplace());
        result.setUpdatedAt(index.getUpdatedAt());
        result.setPlugins(filtered);
        return result;
    }

    /**
     * 判断单个插件条目是否匹配所有过滤条件。
     */
    private static boolean matches(PluginIndexEntry entry, String keyword,
                                   String type, List<String> tags) {
        // 关键词过滤（name + displayName 模糊匹配）
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            boolean nameMatch = entry.getName() != null
                    && entry.getName().toLowerCase().contains(kw);
            boolean displayMatch = entry.getDisplayName() != null
                    && entry.getDisplayName().toLowerCase().contains(kw);
            if (!nameMatch && !displayMatch) {
                return false;
            }
        }

        // 类型过滤
        if (type != null && !type.isBlank()
                && !type.equalsIgnoreCase(entry.getType())) {
            return false;
        }

        // 标签过滤（全部匹配，大小写不敏感）
        if (!tags.isEmpty()) {
            if (entry.getTags() == null) return false;
            List<String> entryTagsLower = entry.getTags().stream()
                    .map(String::toLowerCase)
                    .toList();
            for (String tag : tags) {
                if (!entryTagsLower.contains(tag.trim().toLowerCase())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 比对本地已安装插件与市场最新版本，返回可更新列表。
     * <p>
     * 由用户点击"检查更新"触发。
     * </p>
     *
     * @param index 已拉取的市场索引
     * @return 可更新插件列表（含当前版本、最新版本、下载信息）
     */
    public List<UpdateInfo> checkUpdates(PluginIndex index) {
        List<PluginInfo> installed = pluginInfoRepository.findAll();
        List<UpdateInfo> updates = new ArrayList<>();

        for (PluginInfo local : installed) {
            PluginIndexEntry entry = index.getPlugins().get(local.getPluginName());
            if (entry == null || entry.getVersions() == null || entry.getVersions().isEmpty()) {
                continue;
            }

            String latestVersion = findLatestVersion(entry.getVersions());
            if (latestVersion == null) {
                continue;
            }

            PluginVersionEntry versionEntry = entry.getVersions().get(latestVersion);
            boolean hasUpdate = !latestVersion.equals(local.getVersion());

            updates.add(new UpdateInfo(
                    local.getPluginName(),
                    local.getDisplayName() != null ? local.getDisplayName() : local.getPluginName(),
                    local.getVersion(),
                    latestVersion,
                    hasUpdate,
                    versionEntry != null ? versionEntry.getDownloadUrl() : null,
                    versionEntry != null ? versionEntry.getFileSize() : null,
                    versionEntry != null ? versionEntry.getReleaseNotes() : null
            ));
        }
        return updates;
    }

    // ══════════════════════════════════════════════
    // 插件安装与卸载
    // ══════════════════════════════════════════════

    /**
     * 安装或升级插件。
     * <p>
     * 流程：查找市场索引 → 下载 jar → SHA256 校验 → 保存到 plugin 目录，
     * 更新数据库记录，调用 hot-reload 刷新运行时。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     * @param version    要安装的版本号（传 {@code "latest"} 表示安装最新版）
     * @throws MarketException 任何步骤失败时抛出
     */
    public void install(String pluginName, String version) {
        PluginIndex index = fetchIndex();
        PluginIndexEntry entry = index.getPlugins().get(pluginName);
        if (entry == null) {
            throw new MarketException("市场未找到插件: " + pluginName);
        }

        String targetVersion = LATEST_TAG.equals(version) ? findLatestVersion(entry.getVersions()) : version;
        PluginVersionEntry versionEntry = entry.getVersions().get(targetVersion);
        if (versionEntry == null) {
            throw new MarketException("插件 " + pluginName + " 未找到版本: " + targetVersion);
        }

        log.info("开始下载插件: {} v{} ({} bytes)", pluginName, targetVersion, versionEntry.getFileSize());

        // 确保 plugin 目录存在
        Path jarPath = PLUGIN_DIR.resolve(pluginName + ".jar");
        try {
            Files.createDirectories(PLUGIN_DIR);
        } catch (IOException e) {
            throw new MarketException("无法创建插件目录: " + e.getMessage());
        }

        // 使用 HttpFileDownloader 流式下载到目标路径（带 SSE 进度事件）
        boolean downloadOk = HttpFileDownloader.sendGetForFile(
                versionEntry.getDownloadUrl(), jarPath.toString());
        if (!downloadOk) {
            throw new MarketException("下载插件失败");
        }

        // SHA256 校验（如果索引中提供了校验值）
        if (versionEntry.getSha256() != null && !versionEntry.getSha256().isEmpty()) {
            try {
                byte[] jarBytes = Files.readAllBytes(jarPath);
                String actualSha256 = sha256Hex(jarBytes);
                if (!versionEntry.getSha256().equalsIgnoreCase(actualSha256)) {
                    Files.deleteIfExists(jarPath);
                    throw new MarketException("插件文件校验失败，SHA256 不匹配");
                }
                log.info("SHA256 校验通过");
            } catch (IOException e) {
                throw new MarketException("读取下载的插件文件失败: " + e.getMessage());
            }
        }

        // 更新数据库记录
        PluginInfo info = pluginInfoRepository.findByPluginName(pluginName)
                .orElse(new PluginInfo());
        info.setPluginName(pluginName);
        info.setDisplayName(entry.getDisplayName());
        info.setVersion(targetVersion);
        info.setDescription(entry.getDescription());
        info.setAuthor(entry.getAuthor());
        info.setType(entry.getType());
        info.setDownloadUrl(versionEntry.getDownloadUrl());
        info.setFilePath(jarPath.toString());
        info.setFileSize(versionEntry.getFileSize());
        info.setRepository(entry.getRepository());
        info.setLicense(entry.getLicense());
        info.setHomepage(entry.getHomepage());
        info.setIconUrl(entry.getIconUrl());
        info.setTags(entry.getTags() != null ? String.join(",", entry.getTags()) : null);
        info.setEnabled(true);
        pluginInfoRepository.save(info);

        // 热加载
        reloadPlugins();

        log.info("插件安装完成: {} v{}", pluginName, targetVersion);
    }

    /**
     * 卸载插件。
     * <p>
     * 流程：删除 jar 文件，清理数据库记录，热加载。
     * 如果当前正在使用该插件，自动回退到第一个可用插件。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     * @throws MarketException 卸载失败时抛出
     */
    public void uninstall(String pluginName) {
        Path jarPath = PLUGIN_DIR.resolve(pluginName + ".jar");

        // 删除 jar 文件
        try {
            if (Files.exists(jarPath)) {
                Files.delete(jarPath);
                log.info("插件文件已删除: {}", jarPath);
            }
        } catch (IOException e) {
            log.error("删除插件文件失败: {}", jarPath, e);
            throw new MarketException("删除插件文件失败: " + e.getMessage());
        }

        // 清理数据库
        pluginInfoRepository.findByPluginName(pluginName)
                .ifPresent(info -> {
                    pluginInfoRepository.delete(info);
                    log.info("插件数据库记录已删除: {}", pluginName);
                });

        // 热加载
        reloadPlugins();

        // 如果当前活跃插件就是被卸载的，自动回退
        String currentName = activePlugin.getPluginName();
        if (currentName.equals(pluginName) || currentName.equals("DefaultDrawImagePlugin")) {
            DrawImagePlugin fallback = manager.getFirstPlugin();
            activePlugin.switchTo(fallback);
            log.warn("当前插件 {} 已被卸载，回退到: {} v{}",
                    pluginName, fallback.getPluginName(), fallback.getPluginVersion());
            yamlService.update(config -> config.put(ConfigConstants.PLUGIN_NAME, fallback.getPluginName()));
        }

        log.info("插件卸载完成: {}", pluginName);
    }

    // ══════════════════════════════════════════════
    // 内部方法
    // ══════════════════════════════════════════════

    /**
     * 热加载插件目录，保留当前已选插件的匹配。
     */
    private void reloadPlugins() {
        try {
            manager.loadPlugins("./plugin");
            String currentName = activePlugin.getPluginName();
            DrawImagePlugin plugin = manager.getPluginByName(currentName);
            if (plugin == null) {
                plugin = manager.getFirstPlugin();
                log.warn("当前插件 {} 在热加载后未找到，回退到: {}", currentName, plugin.getPluginName());
            }
            activePlugin.switchTo(plugin);
            log.info("插件热加载完成，共 {} 个插件", manager.getPluginCount());
        } catch (Exception e) {
            log.error("插件热加载失败", e);
            throw new MarketException("热加载插件失败: " + e.getMessage());
        }
    }

    /**
     * 从版本映射中找到最新版本号（按字典序降序取第一个）。
     */
    private static String findLatestVersion(Map<String, PluginVersionEntry> versions) {
        return versions.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * 计算字节数组的 SHA-256 十六进制字符串。
     */
    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 不可用", e);
        }
    }
}
