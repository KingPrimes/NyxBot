package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.pluginmarket.MarketException;
import com.nyx.bot.pluginmarket.PluginIndex;
import com.nyx.bot.pluginmarket.PluginMarketService;
import com.nyx.bot.pluginmarket.UpdateInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PluginMarketController 单元测试")
class PluginMarketControllerTest {

    @Mock
    private PluginMarketService marketService;

    @InjectMocks
    private PluginMarketController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ══════════════════════════════════════════════
    // listMarket()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("listMarket — 成功")
    void listMarketSuccess() {
        PluginIndex index = new PluginIndex();
        index.setSchemaVersion("1.0");
        when(marketService.searchPlugins(null, null, null)).thenReturn(index);

        ApiResponse<?> response = controller.listMarket(null, null, null);

        assertTrue(response.isSuccess());
        assertEquals("获取市场列表成功", response.getMsg());
        assertInstanceOf(PluginIndex.class, response.getData());
    }

    @Test
    @DisplayName("listMarket — 带搜索参数")
    void listMarketWithFilters() {
        PluginIndex index = new PluginIndex();
        when(marketService.searchPlugins("draw", "jar", "native")).thenReturn(index);

        ApiResponse<?> response = controller.listMarket("draw", "jar", "native");

        assertTrue(response.isSuccess());
        verify(marketService).searchPlugins("draw", "jar", "native");
    }

    @Test
    @DisplayName("listMarket — 服务异常返回 500")
    void listMarketError() {
        when(marketService.searchPlugins(any(), any(), any()))
                .thenThrow(new MarketException("网络错误"));

        ApiResponse<?> response = controller.listMarket(null, null, null);

        assertTrue(response.isError());
        assertEquals("获取市场列表失败: 网络错误", response.getMsg());
    }

    // ══════════════════════════════════════════════
    // checkUpdate()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("checkUpdate — 成功")
    void checkUpdateSuccess() {
        PluginIndex index = new PluginIndex();
        when(marketService.fetchIndex()).thenReturn(index);

        UpdateInfo update = new UpdateInfo("p1", "P1", "1.0", "2.0", true, "url", 100L, "notes");
        when(marketService.checkUpdates(index)).thenReturn(List.of(update));

        ApiResponse<?> response = controller.checkUpdate();

        assertTrue(response.isSuccess());
        assertEquals("检查更新完成", response.getMsg());
        List<?> data = (List<?>) response.getData();
        assertEquals(1, data.size());
    }

    @Test
    @DisplayName("checkUpdate — 无更新")
    void checkUpdateNoUpdates() {
        PluginIndex index = new PluginIndex();
        when(marketService.fetchIndex()).thenReturn(index);
        when(marketService.checkUpdates(index)).thenReturn(List.of());

        ApiResponse<?> response = controller.checkUpdate();

        assertTrue(response.isSuccess());
        List<?> data = (List<?>) response.getData();
        assertTrue(data.isEmpty());
    }

    @Test
    @DisplayName("checkUpdate — 服务异常返回 500")
    void checkUpdateError() {
        when(marketService.fetchIndex())
                .thenThrow(new MarketException("无法连接到 GitHub"));

        ApiResponse<?> response = controller.checkUpdate();

        assertTrue(response.isError());
        assertEquals("检查更新失败: 无法连接到 GitHub", response.getMsg());
    }

    // ══════════════════════════════════════════════
    // install()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("install — 成功安装最新版")
    void installSuccess() {
        doNothing().when(marketService).install("test-plugin", "latest");

        ApiResponse<Void> response = controller.install("test-plugin", "latest");

        assertTrue(response.isSuccess());
        assertEquals("插件安装成功: test-plugin", response.getMsg());
        verify(marketService).install("test-plugin", "latest");
    }

    @Test
    @DisplayName("install — 指定版本安装")
    void installSpecificVersion() {
        doNothing().when(marketService).install("test-plugin", "2.0.0");

        ApiResponse<Void> response = controller.install("test-plugin", "2.0.0");

        assertTrue(response.isSuccess());
        verify(marketService).install("test-plugin", "2.0.0");
    }

    @Test
    @DisplayName("install — 默认 version 为 latest")
    void installDefaultVersion() {
        doNothing().when(marketService).install("test-plugin", "latest");

        // defaultValue = "latest" 由 @RequestParam(defaultValue="latest") 处理
        // 测试中显式传入
        controller.install("test-plugin", "latest");

        verify(marketService).install("test-plugin", "latest");
    }

    @Test
    @DisplayName("install — 服务异常返回 500")
    void installError() {
        doThrow(new MarketException("SHA256 校验失败"))
                .when(marketService).install("bad-plugin", "latest");

        ApiResponse<Void> response = controller.install("bad-plugin", "latest");

        assertTrue(response.isError());
        assertEquals("插件安装失败: SHA256 校验失败", response.getMsg());
    }

    // ══════════════════════════════════════════════
    // uninstall()
    // ══════════════════════════════════════════════

    @Test
    @DisplayName("uninstall — 成功卸载")
    void uninstallSuccess() {
        doNothing().when(marketService).uninstall("test-plugin");

        ApiResponse<Void> response = controller.uninstall("test-plugin");

        assertTrue(response.isSuccess());
        assertEquals("插件卸载成功: test-plugin", response.getMsg());
        verify(marketService).uninstall("test-plugin");
    }

    @Test
    @DisplayName("uninstall — 服务异常返回 500")
    void uninstallError() {
        doThrow(new MarketException("删除文件失败"))
                .when(marketService).uninstall("stuck-plugin");

        ApiResponse<Void> response = controller.uninstall("stuck-plugin");

        assertTrue(response.isError());
        assertEquals("插件卸载失败: 删除文件失败", response.getMsg());
    }
}
