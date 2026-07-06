package com.nyx.bot.pluginmarket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.config.LocateYamlService;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.entity.PluginInfo;
import com.nyx.bot.repo.PluginInfoRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpFileDownloader;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.DrawImagePluginManager;
import io.github.kingprimes.SwitchableDrawImagePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PluginMarketService 单元测试")
class PluginMarketServiceTest {

    private PluginInfoRepository pluginInfoRepository;
    private LocateYamlService yamlService;
    private DrawImagePluginManager manager;
    private SwitchableDrawImagePlugin activePlugin;
    private ObjectMapper objectMapper;
    private PluginMarketService service;
    private MockedStatic<HttpUtils> httpUtilsMock;

    private Path tempPluginDir;
    private Path originalPluginDir;
    private Path tempCacheDir;
    private Path originalCacheDir;

    /**
     * HttpUtils.<clinit> 触发 ProxyUtils → SpringUtils.getBean()，
     * 单元测试无 Spring 上下文会 NPE。
     * 在类加载前 mock SpringUtils 预防。
     */
    @BeforeAll
    static void preloadHttpUtils() {
        if (System.getProperty("HttpUtilsPreloaded") == null) {
            try (MockedStatic<SpringUtils> springMock = mockStatic(SpringUtils.class)) {
                LocateYamlService mockYaml = mock(LocateYamlService.class);
                when(mockYaml.load()).thenReturn(new LinkedHashMap<>());
                springMock.when(() -> SpringUtils.getBean((Class<?>) any())).thenReturn(mockYaml);
                Class.forName("com.nyx.bot.utils.http.HttpUtils");
                System.setProperty("HttpUtilsPreloaded", "true");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("HttpUtils 类加载失败", e);
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        pluginInfoRepository = mock(PluginInfoRepository.class);
        yamlService = mock(LocateYamlService.class);
        manager = mock(DrawImagePluginManager.class);
        activePlugin = mock(SwitchableDrawImagePlugin.class);
        objectMapper = mock(ObjectMapper.class);

        // 替换为临时插件目录
        originalPluginDir = PluginMarketService.PLUGIN_DIR;
        tempPluginDir = Files.createTempDirectory("nyxbot-plugin-test");
        PluginMarketService.PLUGIN_DIR = tempPluginDir;

        // 替换为临时磁盘缓存目录
        originalCacheDir = PluginMarketService.CACHE_DIR;
        tempCacheDir = Files.createTempDirectory("nyxbot-market-cache-test");
        PluginMarketService.CACHE_DIR = tempCacheDir;

        service = new PluginMarketService(
                pluginInfoRepository, yamlService, manager, activePlugin, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 恢复插件目录
        PluginMarketService.PLUGIN_DIR = originalPluginDir;
        // 恢复缓存目录
        PluginMarketService.CACHE_DIR = originalCacheDir;

        if (httpUtilsMock != null) {
            httpUtilsMock.close();
        }
        if (tempPluginDir != null) {
            try (var files = Files.walk(tempPluginDir)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
        if (tempCacheDir != null) {
            try (var files = Files.walk(tempCacheDir)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
    }

    // ══════════════════════════════════════════════
    // fetchIndex()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("fetchIndex — 成功拉取并解析市场索引")
    void fetchIndexSuccess() throws Exception {
        String json = "{\"schemaVersion\":\"1.0\",\"plugins\":{}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200, Map.of(), "https://test/cdn"));

        PluginIndex expected = new PluginIndex();
        expected.setSchemaVersion("1.0");
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(expected);

        PluginIndex result = service.fetchIndex();

        assertNotNull(result);
        assertEquals("1.0", result.getSchemaVersion());
        httpUtilsMock.verify(() -> HttpUtils.sendGet(anyString(), anyString(), any()));
    }

    @Test
    @DisplayName("fetchIndex — HTTP 失败且无磁盘兜底时抛 MarketException")
    void fetchIndexHttpError() {
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(500));

        MarketException ex = assertThrows(MarketException.class, service::fetchIndex);
        assertTrue(ex.getMessage().contains("所有数据源不可用且无本地缓存兜底"));
    }

    @Test
    @DisplayName("fetchIndex — JSON 解析失败抛 MarketException")
    void fetchIndexParseError() throws Exception {
        String json = "{invalid}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));
        when(objectMapper.readValue(json, PluginIndex.class))
                .thenThrow(new JsonProcessingException("JSON parse error") {});

        MarketException ex = assertThrows(MarketException.class, service::fetchIndex);
        assertTrue(ex.getMessage().contains("JSON parse error"));
    }

    @Test
    @DisplayName("fetchIndex — Jackson 未填充 name 时自动回填 map key")
    void fetchIndexPopulatesNameFromMapKey() throws Exception {
        // 模拟一个 JSON，其中 entry 的 name 字段为 null（Jackson 不会自动关联 map key）
        String json = "{\"plugins\":{\"my-plugin\":{\"displayName\":\"MyPlugin\"}}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setDisplayName("MyPlugin");

        PluginIndex parsed = new PluginIndex();
        parsed.setPlugins(Map.of("my-plugin", entry));
        when(objectMapper.readValue(eq(json), eq(PluginIndex.class))).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertEquals("my-plugin", result.getPlugins().get("my-plugin").getName());
    }

    // ══════════════════════════════════════════════
    // 磁盘缓存层
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("fetchIndex — 磁盘缓存命中且未超 TTL 时不走网络")
    void fetchIndexDiskCacheHitSkipsNetwork() throws Exception {
        Path indexJson = tempCacheDir.resolve("index.json");
        Files.writeString(indexJson, "{\"schemaVersion\":\"1.0\",\"plugins\":{}}");

        PluginMarketService.CacheMeta meta = new PluginMarketService.CacheMeta(
                "https://test/cdn", java.time.Instant.now().toString(), 32L, "any");
        Path metaJson = tempCacheDir.resolve("index.meta.json");
        new ObjectMapper().writeValue(metaJson.toFile(), meta);

        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenThrow(new AssertionError("磁盘命中时不应发起 HTTP 请求"));

        PluginIndex parsed = new PluginIndex();
        parsed.setSchemaVersion("1.0");
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginMarketService.CacheMeta.class)))
                .thenReturn(meta);
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginIndex.class))).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertEquals("1.0", result.getSchemaVersion());
        httpUtilsMock.verify(() -> HttpUtils.sendGet(anyString(), anyString(), any()), never());
    }

    @Test
    @DisplayName("fetchIndex — 磁盘缓存过期时尝试远程刷新并覆盖写入新缓存")
    void fetchIndexDiskCacheExpiredRefetches() throws Exception {
        Path indexJson = tempCacheDir.resolve("index.json");
        Files.writeString(indexJson, "{\"plugins\":{}}");

        PluginMarketService.CacheMeta staleMeta = new PluginMarketService.CacheMeta(
                "https://test/cdn-old", java.time.Instant.now().minusSeconds(7200).toString(),
                12L, "any");
        // meta 文件真实落盘，但读盘走 mock objectMapper，必须显式存根 readValue(Reader, CacheMeta.class)
        // 才能让 readMeta() 返回 staleMeta，进而让 isFresh() 真正执行过期判断（2h > 1h TTL）
        Path metaJson = tempCacheDir.resolve("index.meta.json");
        new ObjectMapper().writeValue(metaJson.toFile(), staleMeta);

        String freshJson = "{\"schemaVersion\":\"2.0\",\"plugins\":{}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(freshJson, 200, Map.of(), "https://test/cdn-new"));

        PluginIndex parsed = new PluginIndex();
        parsed.setSchemaVersion("2.0");
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginMarketService.CacheMeta.class)))
                .thenReturn(staleMeta);
        when(objectMapper.readValue(freshJson, PluginIndex.class)).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertEquals("2.0", result.getSchemaVersion());
        // 验证磁盘文件被新内容覆盖
        assertEquals(freshJson, Files.readString(indexJson));
    }

    @Test
    @DisplayName("fetchIndex — 多源全失败时降级使用过期磁盘索引并告警")
    void fetchIndexFallbackToStaleDisk() throws Exception {
        Path indexJson = tempCacheDir.resolve("index.json");
        Files.writeString(indexJson, "{\"plugins\":{\"stale\":{}}}");

        PluginMarketService.CacheMeta staleMeta = new PluginMarketService.CacheMeta(
                "https://test/cdn", java.time.Instant.now().minusSeconds(7200).toString(),
                24L, "any");
        // 存根 readValue(Reader, CacheMeta.class) 让 readMeta() 真正返回 staleMeta，
        // 使 isFresh() 走过期分支 — 这样 staleMeta 才不是死代码，且覆盖过期判断逻辑
        Path metaJson = tempCacheDir.resolve("index.meta.json");
        new ObjectMapper().writeValue(metaJson.toFile(), staleMeta);

        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(500));

        PluginIndexEntry entry = new PluginIndexEntry();
        PluginIndex parsed = new PluginIndex();
        parsed.setPlugins(Map.of("stale", entry));
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginMarketService.CacheMeta.class)))
                .thenReturn(staleMeta);
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginIndex.class))).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertNotNull(result);
        assertEquals("stale", result.getPlugins().get("stale").getName());
    }

    @Test
    @DisplayName("fetchIndex — 首次启动无磁盘缓存时走远程拉取并落盘")
    void fetchIndexFirstBootNetworkOnly() throws Exception {
        assertFalse(Files.exists(tempCacheDir.resolve("index.meta.json")));

        String json = "{\"schemaVersion\":\"1.0\",\"plugins\":{}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200, Map.of(), "https://test/cdn"));

        PluginIndex parsed = new PluginIndex();
        parsed.setSchemaVersion("1.0");
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertEquals("1.0", result.getSchemaVersion());
        // writeToDisk 用 Files.writeString 写 index.json（即便 objectMapper.writeValue 写 meta 被 mock 掉）
        assertTrue(Files.exists(tempCacheDir.resolve("index.json")));
    }

    @Test
    @DisplayName("fetchIndex — meta 解析失败视为无缓存，走远程")
    void fetchIndexMetaCorruptFallsBackToNetwork() throws Exception {
        Path metaJson = tempCacheDir.resolve("index.meta.json");
        Files.writeString(metaJson, "{this is not valid json");

        String json = "{\"schemaVersion\":\"1.0\",\"plugins\":{}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200, Map.of(), "https://test/cdn"));

        PluginIndex parsed = new PluginIndex();
        parsed.setSchemaVersion("1.0");
        // 存根 readValue(Reader, CacheMeta.class) 抛 IOException，模拟 readMeta() 真正解析磁盘损坏内容失败。
        // readMeta() 会 catch IOException 并返回 null —— 这才是"meta 解析失败视为无缓存"路径，
        // 而非依赖 mock 的默认 null 返回。
        when(objectMapper.readValue(any(java.io.Reader.class), eq(PluginMarketService.CacheMeta.class)))
                .thenThrow(new IOException("simulated corrupt meta parse"));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(parsed);

        PluginIndex result = service.fetchIndex();
        assertEquals("1.0", result.getSchemaVersion());
    }

    // ══════════════════════════════════════════════
    // searchPlugins()
    // ══════════════════════════════════════════════

    private PluginIndex createTestIndex() {
        PluginIndex index = new PluginIndex();
        index.setSchemaVersion("1.0");
        index.setMarketplace("test");
        index.setUpdatedAt("2026-06-30T00:00:00Z");

        PluginIndexEntry p1 = new PluginIndexEntry();
        p1.setName("plugin-a");
        p1.setDisplayName("插件 A");
        p1.setType("jar");
        p1.setTags(List.of("draw", "native"));

        PluginIndexEntry p2 = new PluginIndexEntry();
        p2.setName("plugin-b");
        p2.setDisplayName("Native 插件 B");
        p2.setType("native");
        p2.setTags(List.of("native", "jna"));

        PluginIndexEntry p3 = new PluginIndexEntry();
        p3.setName("other-plugin");
        p3.setDisplayName("Other");
        p3.setType("jar");
        p3.setTags(List.of("utility"));

        Map<String, PluginIndexEntry> plugins = new LinkedHashMap<>();
        plugins.put("plugin-a", p1);
        plugins.put("plugin-b", p2);
        plugins.put("other-plugin", p3);
        index.setPlugins(plugins);
        return index;
    }

    /** 绕过网络请求，用 mocked 数据构建返回 */
    private void mockFetchIndex(PluginIndex index) throws Exception {
        String json = "{}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);
    }

    /** 模拟 reloadPlugins 内部依赖，防止 NPE */
    private void mockReloadPluginsDeps() {
        DrawImagePlugin anyPlugin = mock(DrawImagePlugin.class);
        when(anyPlugin.getPluginName()).thenReturn("mock-plugin");
        when(anyPlugin.getPluginVersion()).thenReturn("1.0.0");
        when(activePlugin.getPluginName()).thenReturn("mock-plugin");
        when(manager.getPluginByName(anyString())).thenReturn(anyPlugin);
        when(manager.getFirstPlugin()).thenReturn(anyPlugin);
        when(manager.getPluginCount()).thenReturn(1);
    }

    @Test
    @DisplayName("searchPlugins — 无过滤返回全部")
    void searchPluginsNoFilter() throws Exception {
        PluginIndex index = createTestIndex();
        mockFetchIndex(index);

        PluginIndex result = service.searchPlugins(null, null, null);
        assertEquals(3, result.getPlugins().size());
    }

    @Test
    @DisplayName("searchPlugins — keyword 匹配 name")
    void searchPluginsKeywordMatchName() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins("plugin-a", null, null);
        assertEquals(1, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-a"));
    }

    @Test
    @DisplayName("searchPlugins — keyword 匹配 displayName")
    void searchPluginsKeywordMatchDisplay() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins("插件", null, null);
        assertEquals(2, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-a"));
        assertTrue(result.getPlugins().containsKey("plugin-b"));
    }

    @Test
    @DisplayName("searchPlugins — keyword 大小写不敏感")
    void searchPluginsKeywordCaseInsensitive() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins("NATIVE", null, null);
        assertEquals(1, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-b"));
    }

    @Test
    @DisplayName("searchPlugins — keyword 无匹配返回空")
    void searchPluginsKeywordNoMatch() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins("notexist", null, null);
        assertTrue(result.getPlugins().isEmpty());
    }

    @Test
    @DisplayName("searchPlugins — type 过滤")
    void searchPluginsTypeFilter() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins(null, "native", null);
        assertEquals(1, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-b"));
    }

    @Test
    @DisplayName("searchPlugins — tags 过滤（全部匹配）")
    void searchPluginsTagsFilter() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins(null, null, "native");
        assertEquals(2, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-a"));
        assertTrue(result.getPlugins().containsKey("plugin-b"));
    }

    @Test
    @DisplayName("searchPlugins — 多标签 AND 过滤")
    void searchPluginsTagsAnd() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins(null, null, "draw,native");
        assertEquals(1, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("plugin-a"));
    }

    @Test
    @DisplayName("searchPlugins — 组合过滤（name + type）")
    void searchPluginsCombined() throws Exception {
        mockFetchIndex(createTestIndex());
        PluginIndex result = service.searchPlugins("Other", "jar", null);
        assertEquals(1, result.getPlugins().size());
        assertTrue(result.getPlugins().containsKey("other-plugin"));
    }

    // ══════════════════════════════════════════════
    // checkUpdates()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("checkUpdates — 全部最新，列表不含更新")
    void checkUpdatesAllCurrent() {
        PluginIndex index = createTestIndex();

        PluginInfo info = new PluginInfo();
        info.setPluginName("plugin-a");
        info.setDisplayName("插件 A");
        info.setVersion("2.0.0");

        PluginVersionEntry v1 = new PluginVersionEntry();
        index.getPlugins().get("plugin-a").setVersions(Map.of("2.0.0", v1));

        when(pluginInfoRepository.findAll()).thenReturn(List.of(info));

        List<UpdateInfo> updates = service.checkUpdates(index);
        assertEquals(1, updates.size());
        assertFalse(updates.get(0).isHasUpdate());
    }

    @Test
    @DisplayName("checkUpdates — 有可用更新")
    void checkUpdatesHasUpdate() {
        PluginIndex index = createTestIndex();

        PluginInfo info = new PluginInfo();
        info.setPluginName("plugin-a");
        info.setDisplayName("插件 A");
        info.setVersion("1.0.0");

        PluginVersionEntry v1 = new PluginVersionEntry();
        PluginVersionEntry v2 = new PluginVersionEntry();
        index.getPlugins().get("plugin-a").setVersions(Map.of("1.0.0", v1, "2.0.0", v2));

        when(pluginInfoRepository.findAll()).thenReturn(List.of(info));

        List<UpdateInfo> updates = service.checkUpdates(index);
        assertEquals(1, updates.size());
        UpdateInfo u = updates.get(0);
        assertEquals("plugin-a", u.getPluginName());
        assertEquals("1.0.0", u.getCurrentVersion());
        assertEquals("2.0.0", u.getLatestVersion());
        assertTrue(u.isHasUpdate());
    }

    @Test
    @DisplayName("checkUpdates — 语义版本正确排序（2.10.0 > 2.9.0）")
    void checkUpdatesSemVerOrder() {
        PluginIndex index = createTestIndex();

        PluginVersionEntry v9 = new PluginVersionEntry();
        PluginVersionEntry v10 = new PluginVersionEntry();
        index.getPlugins().get("plugin-a").setVersions(Map.of("2.9.0", v9, "2.10.0", v10));

        PluginInfo info = new PluginInfo();
        info.setPluginName("plugin-a");
        info.setDisplayName("插件 A");
        info.setVersion("2.9.0");

        when(pluginInfoRepository.findAll()).thenReturn(List.of(info));

        List<UpdateInfo> updates = service.checkUpdates(index);
        assertEquals(1, updates.size());
        assertEquals("2.10.0", updates.get(0).getLatestVersion());
        assertTrue(updates.get(0).isHasUpdate());
    }

    @Test
    @DisplayName("checkUpdates — 本地插件在市场索引中不存在，跳过")
    void checkUpdatesPluginNotInMarket() {
        PluginIndex index = createTestIndex();

        PluginInfo info = new PluginInfo();
        info.setPluginName("not-in-market");
        info.setVersion("1.0.0");

        when(pluginInfoRepository.findAll()).thenReturn(List.of(info));

        List<UpdateInfo> updates = service.checkUpdates(index);
        assertTrue(updates.isEmpty());
    }

    @Test
    @DisplayName("checkUpdates — 多个已安装插件，部分有更新")
    void checkUpdatesMultiplePartial() {
        PluginIndex index = createTestIndex();

        PluginVersionEntry va1 = new PluginVersionEntry();
        PluginVersionEntry va2 = new PluginVersionEntry();
        index.getPlugins().get("plugin-a").setVersions(Map.of("1.0.0", va1, "2.0.0", va2));

        PluginVersionEntry vb1 = new PluginVersionEntry();
        index.getPlugins().get("plugin-b").setVersions(Map.of("1.0.0", vb1));

        PluginInfo infoA = new PluginInfo();
        infoA.setPluginName("plugin-a");
        infoA.setDisplayName("插件 A");
        infoA.setVersion("1.0.0");

        PluginInfo infoB = new PluginInfo();
        infoB.setPluginName("plugin-b");
        infoB.setDisplayName("Native 插件 B");
        infoB.setVersion("1.0.0");

        when(pluginInfoRepository.findAll()).thenReturn(List.of(infoA, infoB));

        List<UpdateInfo> updates = service.checkUpdates(index);
        assertEquals(2, updates.size());
        UpdateInfo ua = updates.stream().filter(UpdateInfo::isHasUpdate).findFirst().orElseThrow();
        assertEquals("plugin-a", ua.getPluginName());
        assertEquals("2.0.0", ua.getLatestVersion());
        UpdateInfo ub = updates.stream().filter(u -> !u.isHasUpdate()).findFirst().orElseThrow();
        assertEquals("plugin-b", ub.getPluginName());
    }

    // ══════════════════════════════════════════════
    // install() — 错误路径
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("install — 市场找不到插件抛异常")
    void installPluginNotFound() throws Exception {
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body("{}", 200));

        PluginIndex emptyIndex = new PluginIndex();
        emptyIndex.setPlugins(Map.of());
        when(objectMapper.readValue("{}", PluginIndex.class)).thenReturn(emptyIndex);

        MarketException ex = assertThrows(MarketException.class,
                () -> service.install("unknown", "latest"));
        assertTrue(ex.getMessage().contains("未找到"));
    }

    @Test
    @DisplayName("install — 版本不存在抛异常")
    void installVersionNotFound() throws Exception {
        String json = "{\"plugins\":{\"test\":{}}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("test");
        entry.setVersions(Map.of("1.0.0", new PluginVersionEntry()));

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        MarketException ex = assertThrows(MarketException.class,
                () -> service.install("test", "9.9.9"));
        assertTrue(ex.getMessage().contains("未找到版本"));
    }

    @Test
    @DisplayName("install — 插件无可用版本抛异常")
    void installEmptyVersions() throws Exception {
        String json = "{\"plugins\":{\"test\":{}}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("test");
        entry.setVersions(Map.of()); // 空版本映射

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        MarketException ex = assertThrows(MarketException.class,
                () -> service.install("test", "latest"));
        assertTrue(ex.getMessage().contains("暂无可用版本"));
    }

    @Test
    @DisplayName("install — 插件版本映射为 null 抛异常")
    void installNullVersions() throws Exception {
        String json = "{\"plugins\":{\"test\":{}}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("test");
        entry.setVersions(null); // 版本映射为 null

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        MarketException ex = assertThrows(MarketException.class,
                () -> service.install("test", "latest"));
        assertTrue(ex.getMessage().contains("暂无可用版本"));
    }

    @Test
    @DisplayName("install — 下载失败抛异常")
    void installDownloadFailed() throws Exception {
        // mock fetchIndex
        String json = "{}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginVersionEntry ve = new PluginVersionEntry();
        ve.setDownloadUrl("http://example.com/test-1.0.0.jar");
        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("test");
        entry.setDisplayName("Test");
        entry.setVersions(Map.of("1.0.0", ve));

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        // mock 下载失败
        try (MockedStatic<HttpFileDownloader> downloaderMock = mockStatic(HttpFileDownloader.class)) {
            downloaderMock.when(() -> HttpFileDownloader.sendGetForFile(anyString(), anyString()))
                    .thenReturn(false);

            MarketException ex = assertThrows(MarketException.class,
                    () -> service.install("test", "1.0.0"));
            assertTrue(ex.getMessage().contains("下载插件失败"));
        }
    }

    @Test
    @DisplayName("install — SHA256 不匹配抛异常并清理文件")
    void installSha256Mismatch() throws Exception {
        // mock fetchIndex
        String json = "{}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        // 创建目标 jar 文件（模拟下载好的文件）
        Path jarPath = tempPluginDir.resolve("sha-test.jar");
        Files.writeString(jarPath, "hello world");

        // 计算实际 SHA256
        String actualSha256 = sha256Hex("hello world".getBytes());
        // 提供一个不同的 SHA256
        String wrongSha256 = "0000000000000000000000000000000000000000000000000000000000000000";

        PluginVersionEntry ve = new PluginVersionEntry();
        ve.setDownloadUrl("http://example.com/sha-test-1.0.0.jar");
        ve.setSha256(wrongSha256); // 不匹配的校验值
        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("sha-test");
        entry.setDisplayName("SHATest");
        entry.setVersions(Map.of("1.0.0", ve));

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("sha-test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        // mock 下载成功（文件已存在）
        try (MockedStatic<HttpFileDownloader> downloaderMock = mockStatic(HttpFileDownloader.class)) {
            downloaderMock.when(() -> HttpFileDownloader.sendGetForFile(anyString(), anyString()))
                    .thenReturn(true);

            MarketException ex = assertThrows(MarketException.class,
                    () -> service.install("sha-test", "1.0.0"));
            assertTrue(ex.getMessage().contains("SHA256 不匹配"));

            // 验证校验失败后文件被清理
            assertFalse(Files.exists(jarPath));
        }
    }

    @Test
    @DisplayName("install — SHA256 为空时跳过校验")
    void installSkipSha256WhenNull() throws Exception {
        String json = "{}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(anyString(), anyString(), any()))
                .thenReturn(new HttpUtils.Body(json, 200));

        // mock 下载并创建文件
        Path jarPath = tempPluginDir.resolve("no-sha-test.jar");
        Files.writeString(jarPath, "some content");

        PluginVersionEntry ve = new PluginVersionEntry();
        ve.setDownloadUrl("http://example.com/no-sha-test-1.0.0.jar");
        ve.setSha256(null); // 无 SHA256
        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("no-sha-test");
        entry.setDisplayName("NoSHA");
        entry.setVersions(Map.of("1.0.0", ve));

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("no-sha-test", entry));
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(index);

        mockReloadPluginsDeps();
        when(pluginInfoRepository.findByPluginName("no-sha-test"))
                .thenReturn(Optional.empty());

        try (MockedStatic<HttpFileDownloader> downloaderMock = mockStatic(HttpFileDownloader.class)) {
            downloaderMock.when(() -> HttpFileDownloader.sendGetForFile(anyString(), anyString()))
                    .thenReturn(true);

            assertDoesNotThrow(() -> service.install("no-sha-test", "1.0.0"));

            verify(pluginInfoRepository).save(any(PluginInfo.class));
        }
    }

    @Test
    @DisplayName("install(String, String, PluginIndex) — index 为 null 时抛 MarketException")
    void installWithNullIndexThrowsMarketException() {
        // 无需 mock 网络：null 检查在任何 IO 之前触发
        MarketException ex = assertThrows(
                MarketException.class,
                () -> service.install("dummy-plugin", "1.0.0", null)
        );
        // 文案与生产代码 PluginMarketService#install 的 null 守卫一致
        assertTrue(ex.getMessage().contains("市场索引不能为空"));
    }

    @Test
    @DisplayName("install(String, String, PluginIndex) — 复用显式 PluginIndex 正常安装")
    void installWithExplicitIndexSucceeds() throws Exception {
        // 复用 installSkipSha256WhenNull 的正向路径构造方式，但走三参数重载以锁定行为
        Path jarPath = tempPluginDir.resolve("explicit-index-test.jar");
        Files.writeString(jarPath, "some content");

        PluginVersionEntry ve = new PluginVersionEntry();
        ve.setDownloadUrl("http://example.com/explicit-index-test-1.0.0.jar");
        ve.setSha256(null); // 跳过校验，聚焦于重载路径
        PluginIndexEntry entry = new PluginIndexEntry();
        entry.setName("explicit-index-test");
        entry.setDisplayName("ExplicitIndex");
        entry.setVersions(Map.of("1.0.0", ve));

        PluginIndex index = new PluginIndex();
        index.setPlugins(Map.of("explicit-index-test", entry));

        mockReloadPluginsDeps();
        when(pluginInfoRepository.findByPluginName("explicit-index-test"))
                .thenReturn(Optional.empty());

        try (MockedStatic<HttpFileDownloader> downloaderMock = mockStatic(HttpFileDownloader.class)) {
            downloaderMock.when(() -> HttpFileDownloader.sendGetForFile(anyString(), anyString()))
                    .thenReturn(true);

            assertDoesNotThrow(() -> service.install("explicit-index-test", "1.0.0", index));

            // 断言正向路径：DB 记录被保存 + 热加载被调用
            verify(pluginInfoRepository).save(any(PluginInfo.class));
            verify(manager).loadPlugins("./plugin");
        }
    }

    // ══════════════════════════════════════════════
    // uninstall()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("uninstall — 不存在的文件不报错")
    void uninstallNonExistentFile() {
        mockReloadPluginsDeps();
        when(pluginInfoRepository.findByPluginName("no-such-plugin"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.uninstall("no-such-plugin"));
    }

    @Test
    @DisplayName("uninstall — 清理数据库并热加载")
    void uninstallSuccess() {
        mockReloadPluginsDeps();
        PluginInfo info = new PluginInfo();
        info.setPluginName("test-plugin");
        info.setVersion("1.0.0");

        when(pluginInfoRepository.findByPluginName("test-plugin"))
                .thenReturn(Optional.of(info));
        when(activePlugin.getPluginName()).thenReturn("other-plugin");

        assertDoesNotThrow(() -> service.uninstall("test-plugin"));

        verify(pluginInfoRepository).delete(info);
        verify(manager).loadPlugins("./plugin");
    }

    @Test
    @DisplayName("uninstall — 卸载当前活跃插件时自动回退")
    void uninstallActivePlugin() {
        mockReloadPluginsDeps();
        PluginInfo info = new PluginInfo();
        info.setPluginName("active-plugin");
        info.setVersion("1.0.0");

        DrawImagePlugin fallbackPlugin = mock(DrawImagePlugin.class);
        when(fallbackPlugin.getPluginName()).thenReturn("fallback-plugin");
        when(fallbackPlugin.getPluginVersion()).thenReturn("1.0.0");

        when(pluginInfoRepository.findByPluginName("active-plugin"))
                .thenReturn(Optional.of(info));
        when(activePlugin.getPluginName()).thenReturn("active-plugin");
        when(manager.getPluginByName("active-plugin")).thenReturn(null);
        when(manager.getFirstPlugin()).thenReturn(fallbackPlugin);

        assertDoesNotThrow(() -> service.uninstall("active-plugin"));

        verify(activePlugin, atLeast(1)).switchTo(fallbackPlugin);
        verify(pluginInfoRepository).delete(info);
    }

    @Test
    @DisplayName("uninstall — 空插件名抛异常")
    void uninstallEmptyName() {
        assertThrows(MarketException.class, () -> service.uninstall(""));
        assertThrows(MarketException.class, () -> service.uninstall("  "));
        assertThrows(MarketException.class, () -> service.uninstall(null));
    }

    @Test
    @DisplayName("uninstall — 路径穿越防护")
    void uninstallPathTraversal() {
        assertThrows(MarketException.class, () -> service.uninstall("../etc/passwd"));
        assertThrows(MarketException.class, () -> service.uninstall("plugin/../../bad"));
    }

    // ══════════════════════════════════════════════
    // 工具方法
    // ══════════════════════════════════════════════

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
