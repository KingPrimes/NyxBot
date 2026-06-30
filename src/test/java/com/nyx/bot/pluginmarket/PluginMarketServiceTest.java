package com.nyx.bot.pluginmarket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.config.LocateYamlService;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.entity.PluginInfo;
import com.nyx.bot.repo.PluginInfoRepository;
import com.nyx.bot.utils.SpringUtils;
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
import java.util.Comparator;
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
                // 触发 HttpUtils 类加载（静态初始化只执行一次）
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

        tempPluginDir = Files.createTempDirectory("nyxbot-plugin-test");

        service = new PluginMarketService(
                pluginInfoRepository, yamlService, manager, activePlugin, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
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
    }

    // ══════════════════════════════════════════════
    // fetchIndex()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("fetchIndex — 成功拉取并解析市场索引")
    void fetchIndexSuccess() throws Exception {
        String json = "{\"schemaVersion\":\"1.0\",\"plugins\":{}}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
                .thenReturn(new HttpUtils.Body(json, 200));

        PluginIndex expected = new PluginIndex();
        expected.setSchemaVersion("1.0");
        when(objectMapper.readValue(json, PluginIndex.class)).thenReturn(expected);

        PluginIndex result = service.fetchIndex();

        assertNotNull(result);
        assertEquals("1.0", result.getSchemaVersion());
        httpUtilsMock.verify(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX));
    }

    @Test
    @DisplayName("fetchIndex — HTTP 失败抛 MarketException")
    void fetchIndexHttpError() {
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
                .thenReturn(new HttpUtils.Body(500));

        MarketException ex = assertThrows(MarketException.class, service::fetchIndex);
        assertTrue(ex.getMessage().contains("HTTP 500"));
    }

    @Test
    @DisplayName("fetchIndex — JSON 解析失败抛 MarketException")
    void fetchIndexParseError() throws Exception {
        String json = "{invalid}";
        httpUtilsMock = mockStatic(HttpUtils.class);
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
                .thenReturn(new HttpUtils.Body(json, 200));
        when(objectMapper.readValue(json, PluginIndex.class))
                .thenThrow(new JsonProcessingException("JSON parse error") {});

        MarketException ex = assertThrows(MarketException.class, service::fetchIndex);
        assertTrue(ex.getMessage().contains("JSON parse error"));
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
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
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
        // "Other" 只匹配 other-plugin 的 displayName，加上 type=jar 进一步确认
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
        // plugin-a 有更新
        UpdateInfo ua = updates.stream().filter(UpdateInfo::isHasUpdate).findFirst().orElseThrow();
        assertEquals("plugin-a", ua.getPluginName());
        assertEquals("2.0.0", ua.getLatestVersion());
        // plugin-b 无更新
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
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
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
        httpUtilsMock.when(() -> HttpUtils.sendGet(ApiUrl.PLUGIN_MARKET_INDEX))
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
        // 模拟热加载后通过名称找不到已卸载的插件
        when(manager.getPluginByName("active-plugin")).thenReturn(null);
        // 回退到第一个可用插件
        when(manager.getFirstPlugin()).thenReturn(fallbackPlugin);

        assertDoesNotThrow(() -> service.uninstall("active-plugin"));

        verify(activePlugin, atLeast(1)).switchTo(fallbackPlugin);
        verify(pluginInfoRepository).delete(info);
    }
}
