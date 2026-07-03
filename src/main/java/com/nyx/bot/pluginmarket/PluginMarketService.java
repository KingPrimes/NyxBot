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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    /** 插件目录路径（package-private 以供单元测试替换为临时目录） */
    static Path PLUGIN_DIR = Path.of("./plugin");

    /**
     * 磁盘缓存目录（package-private 以供单元测试替换为临时目录）。
     * <p>
     * 索引原文落到 {@code index.json}，元数据落到 {@code index.meta.json}。
     * 与 H2/locate.yaml 同处 {@code ./data} 域，符合项目数据持久化集中策略。
     * </p>
     */
    static Path CACHE_DIR = Path.of("./data/plugin-market-cache");

    private static Path indexJson() {
        return CACHE_DIR.resolve("index.json");
    }

    private static Path indexMeta() {
        return CACHE_DIR.resolve("index.meta.json");
    }

    private static final String LATEST_TAG = "latest";

    /**
     * 磁盘缓存有效期：1 小时。
     * <p>
     * 索引变更低频，磁盘可承受更长 TTL 以显著降低 CDN 请求次数。
     * 超期后仍会尝试远程刷新；远程失败时降级使用磁盘过期索引兜底。
     * </p>
     */
    private static final Duration INDEX_TTL = Duration.ofHours(1);

    /**
     * 磁盘缓存元数据：几十字节，记录上次成功拉取的源、时间戳、大小、SHA-256。
     * <p>
     * 永远不进 Cache2k 内存区，避免大索引占用堆。重启后从 meta 文件直接读取。
     * </p>
     */
    record CacheMeta(String sourceUrl, String fetchedAtIso, long fileSize, String sha256) {
    }

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
     * 从多源 CDN 拉取插件市场索引并解析（磁盘缓存层，不占堆）。
     * <p>
     * 流程：
     * <ol>
     *   <li>读取磁盘 meta 元数据；若 {@code index.json} 存在且未超 TTL，流式读盘返回；</li>
     *   <li>否则尝试多源远程拉取（jsDelivr CDN 优先，GitHub raw 兜底），写入磁盘后解析；</li>
     *   <li>远程全部失败时，若磁盘有过期索引，降级使用并 {@code warn} 告警；无本地缓存则抛异常。</li>
     * </ol>
     * 索引原文始终落磁盘，绝不在 Cache2k 内存区域驻留，避免大文件导致堆压力。
     * </p>
     *
     * @return 解析后的插件市场索引
     * @throws MarketException 所有数据源都失败、无本地缓存兜底或 JSON 解析失败时抛出
     */
    public PluginIndex fetchIndex() {
        CacheMeta meta = readMeta();
        Path indexJson = indexJson();
        boolean diskHasIndex = Files.exists(indexJson);

        // 1) 磁盘未过 TTL：流式读盘返回（最优路径，零网络、零堆长期占用）
        if (diskHasIndex && meta != null && isFresh(meta)) {
            log.debug("插件市场索引磁盘缓存命中: fetchedAt={}", meta.fetchedAtIso());
            return readFromDisk(indexJson);
        }

        // 2) 尝试远程刷新
        HttpUtils.Body body = tryFetchFromSources();
        if (body != null && body.is2xxSuccessful() && body.body() != null) {
            String sourceUrl = body.url();
            writeToDisk(body, sourceUrl);
            return parseIndex(body.body());
        }

        // 3) 远程失败，降级使用过期磁盘索引兜底
        if (diskHasIndex) {
            log.warn("所有数据源不可用，降级使用磁盘过期索引（fetchedAt={}，源={}）",
                    meta != null ? meta.fetchedAtIso() : "未知",
                    meta != null ? meta.sourceUrl() : "未知");
            return readFromDisk(indexJson);
        }

        throw new MarketException("获取市场索引失败: 所有数据源不可用且无本地缓存兜底");
    }

    /**
     * 判断磁盘缓存是否仍在有效期内。
     */
    private static boolean isFresh(CacheMeta meta) {
        try {
            Instant fetchedAt = Instant.parse(meta.fetchedAtIso());
            return Duration.between(fetchedAt, Instant.now()).compareTo(INDEX_TTL) < 0;
        } catch (Exception e) {
            log.warn("无法解析 meta.fetchedAt={}，视为失效", meta.fetchedAtIso());
            return false;
        }
    }

    /**
     * 串行尝试多源 CDN URL，返回首个成功的响应；全部失败返回 null。
     * <p>
     * 刻意使用 {@link HttpUtils#sendGet(String, String, Map)} 三参数零缓存版，
     * 让插件市场完全脱离 {@code http-response} Cache2k 内存缓存区。
     * </p>
     */
    private HttpUtils.Body tryFetchFromSources() {
        List<String> urls = ApiUrl.pluginMarketIndexUrls();
        String lastFailedUrl = null;
        int lastCode = -1;
        for (String url : urls) {
            HttpUtils.Body body = HttpUtils.sendGet(url, "", null);
            if (body.is2xxSuccessful() && body.body() != null) {
                log.info("插件市场索引从 {} 获取成功", url);
                return body;
            }
            lastFailedUrl = url;
            lastCode = body.code();
            log.warn("插件市场数据源 {} 失败 HTTP {}", url, body.code());
        }
        log.warn("插件市场多源拉取全部失败，最后尝试 {} (HTTP {})",
                lastFailedUrl != null ? lastFailedUrl : "未知", lastCode);
        return null;
    }

    // ══════════════════════════════════════════════
    // 磁盘缓存读写（原子写入模式：tmp → Files.move(ATOMIC_MOVE)）
    // ══════════════════════════════════════════════

    /**
     * 流式读取磁盘索引并解析。读失败抛 MarketException。
     */
    private PluginIndex readFromDisk(Path indexJson) {
        try (var reader = Files.newBufferedReader(indexJson, StandardCharsets.UTF_8)) {
            PluginIndex index = objectMapper.readValue(reader, PluginIndex.class);
            backfillNames(index);
            return index;
        } catch (IOException e) {
            log.error("读取磁盘索引失败: {}", indexJson, e);
            throw new MarketException("读取磁盘索引失败: " + e.getMessage());
        }
    }

    /**
     * 解析索引字符串（远程路径专用），并回填 map key 到 entry.name。
     */
    private PluginIndex parseIndex(String json) {
        try {
            PluginIndex index = objectMapper.readValue(json, PluginIndex.class);
            backfillNames(index);
            return index;
        } catch (IOException e) {
            log.error("解析市场索引 JSON 失败", e);
            throw new MarketException("解析市场索引数据失败: " + e.getMessage());
        }
    }

    /**
     * Jackson 反序列化 {@code Map<String, PluginIndexEntry>} 时，map key 与 entry.name 无关联，
     * 手动回填确保 {@link PluginIndexEntry#getName()} 可用。
     */
    private void backfillNames(PluginIndex index) {
        if (index != null && index.getPlugins() != null) {
            index.getPlugins().forEach((key, entry) -> {
                if (entry.getName() == null) {
                    entry.setName(key);
                }
            });
        }
    }

    /**
     * 将远程响应原子写入磁盘：先写到 {@code index.json.tmp}，再 {@code Files.move} 替换目标，
     * 同步更新 {@code index.meta.json}。读者永远看到完整文件，不受半截写影响。
     */
    private void writeToDisk(HttpUtils.Body body, String sourceUrl) {
        try {
            Files.createDirectories(CACHE_DIR);
            Path indexJson = indexJson();
            Path tmp = indexJson.resolveSibling("index.json.tmp");
            Files.writeString(tmp, body.body(), StandardCharsets.UTF_8);
            atomicMove(tmp, indexJson);

            long size = Files.size(indexJson);
            String sha = sha256Hex(body.body().getBytes(StandardCharsets.UTF_8));
            writeMeta(new CacheMeta(sourceUrl, Instant.now().toString(), size, sha));
        } catch (IOException e) {
            log.warn("写入磁盘缓存失败，下次仍走远程: {}", e.getMessage());
            // 写盘失败不影响功能：本次响应仍会返回给调用方
        }
    }

    /**
     * 原子移动 tmp → 目标，跨平台兼容：优先 ATOMIC_MOVE+REPLACE_EXISTING，
     * 失败回退到 REPLACE_EXISTING（仍是单步替换，仅失去崩溃原子性）。
     */
    private static void atomicMove(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (UnsupportedOperationException atomicNotSupported) {
            log.debug("ATOMIC_MOVE 不支持，回退 REPLACE_EXISTING: {}", target);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 读取磁盘 meta；文件缺失或解析失败时返回 null（视为无缓存）。
     */
    private CacheMeta readMeta() {
        Path meta = indexMeta();
        if (!Files.exists(meta)) {
            return null;
        }
        try (var reader = Files.newBufferedReader(meta, StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, CacheMeta.class);
        } catch (IOException e) {
            log.warn("读取缓存 meta 失败，视为无缓存: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 原子写入 meta 文件（tmp → move）。
     */
    private void writeMeta(CacheMeta meta) throws IOException {
        Path metaPath = indexMeta();
        Path tmp = metaPath.resolveSibling("index.meta.json.tmp");
        objectMapper.writeValue(tmp.toFile(), meta);
        atomicMove(tmp, metaPath);
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
     * 内部调用 {@link #fetchIndex()} 获取市场索引后委托给
     * {@link #install(String, String, PluginIndex)}。若调用方已持有有效索引，
     * 请直接使用三参数重载以避免重复拉取。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     * @param version    要安装的版本号（传 {@code "latest"} 表示安装最新版）
     * @throws MarketException 任何步骤失败时抛出
     */
    public void install(String pluginName, String version) {
        install(pluginName, version, fetchIndex());
    }

    /**
     * 安装或升级插件（复用已有市场索引，避免重复拉取）。
     * <p>
     * 流程：从传入索引查找插件 → 下载 jar → SHA256 校验 → 保存到 plugin 目录，
     * 更新数据库记录，调用 hot-reload 刷新运行时。
     * </p>
     *
     * @param pluginName 插件唯一标识名
     * @param version    要安装的版本号（传 {@code "latest"} 表示安装最新版）
     * @param index      调用方已经拉取的市场索引（必须非 null）
     * @throws MarketException 任何步骤失败时抛出
     */
    public void install(String pluginName, String version, PluginIndex index) {
        if (index == null) {
            throw new MarketException("市场索引不能为空");
        }
        PluginIndexEntry entry = index.getPlugins().get(pluginName);
        if (entry == null) {
            throw new MarketException("市场未找到插件: " + pluginName);
        }

        Map<String, PluginVersionEntry> versions = entry.getVersions();
        if (versions == null || versions.isEmpty()) {
            throw new MarketException("插件 " + pluginName + " 暂无可用版本");
        }

        String targetVersion = LATEST_TAG.equals(version) ? findLatestVersion(versions) : version;
        if (targetVersion == null) {
            throw new MarketException("插件 " + pluginName + " 无法确定最新版本");
        }
        PluginVersionEntry versionEntry = versions.get(targetVersion);
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
        // 路径穿越防护：校验插件名、确保路径在 PLUGIN_DIR 内
        validatePluginName(pluginName);
        Path jarPath = PLUGIN_DIR.resolve(pluginName + ".jar").normalize();
        if (!jarPath.startsWith(PLUGIN_DIR.normalize())) {
            throw new MarketException("非法的插件名称: " + pluginName);
        }

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
        if (currentName.equals(pluginName)) {
            DrawImagePlugin fallback = manager.getFirstPlugin();
            if (fallback != null) {
                activePlugin.switchTo(fallback);
                log.warn("当前插件 {} 已被卸载，回退到: {} v{}",
                        pluginName, fallback.getPluginName(), fallback.getPluginVersion());
                yamlService.update(config -> config.put(ConfigConstants.PLUGIN_NAME, fallback.getPluginName()));
            } else {
                log.warn("当前插件 {} 已被卸载，且无可用回退插件", pluginName);
            }
        }

        log.info("插件卸载完成: {}", pluginName);
    }

    /**
     * 校验插件名称合法性，防止路径穿越。
     */
    private static void validatePluginName(String name) {
        if (name == null || name.isBlank()) {
            throw new MarketException("插件名称不能为空");
        }
        if (!name.matches("[\\w.-]+")) {
            throw new MarketException("插件名称包含非法字符: " + name);
        }
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
                if (plugin != null) {
                    log.warn("当前插件 {} 在热加载后未找到，回退到: {}", currentName, plugin.getPluginName());
                }
            }
            if (plugin != null) {
                activePlugin.switchTo(plugin);
                log.info("插件热加载完成，共 {} 个插件", manager.getPluginCount());
            } else {
                log.warn("插件热加载后无可用插件");
            }
        } catch (Exception e) {
            log.error("插件热加载失败", e);
            throw new MarketException("热加载插件失败: " + e.getMessage());
        }
    }

    /**
     * 从版本映射中找到最新版本号（按语义版本分段比较）。
     * <p>
     * 将 "2.9.0" 和 "2.10.0" 按数字分段比较，避免字典序误判。
     * </p>
     */
    private static String findLatestVersion(Map<String, PluginVersionEntry> versions) {
        return versions.keySet().stream()
                .max(PluginMarketService::compareSemVer)
                .orElse(null);
    }

    /** 语义版本比较：按 "." 分段、每段按数字比较 */
    private static int compareSemVer(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");
        int len = Math.max(partsA.length, partsB.length);
        for (int i = 0; i < len; i++) {
            int numA = i < partsA.length ? tryParseInt(partsA[i], 0) : 0;
            int numB = i < partsB.length ? tryParseInt(partsB[i], 0) : 0;
            if (numA != numB) return Integer.compare(numA, numB);
        }
        return 0;
    }

    private static int tryParseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
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
