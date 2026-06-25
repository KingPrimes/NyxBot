package com.nyx.bot.common.core;

import com.nyx.bot.common.config.ConfigConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NyxConfig 序列化/反序列化方法测试
 */
class NyxConfigTest {

    @Test
    @DisplayName("fromMap — 完整配置映射正确")
    void testFromMapFull() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(ConfigConstants.SERVER_PORT, 9090);
        data.put(ConfigConstants.IS_SERVER_OR_CLIENT, false);
        data.put(ConfigConstants.WS_SERVER_URL, "/custom/ws");
        data.put(ConfigConstants.WS_CLIENT_URL, "ws://custom:3001");
        data.put(ConfigConstants.TOKEN, "test-token");
        data.put(ConfigConstants.HTTP_PROXY, "http://proxy:7890");
        data.put(ConfigConstants.SOCKS_PROXY, "socks5://proxy:7891");
        data.put(ConfigConstants.PROXY_USER, "user1");
        data.put(ConfigConstants.PROXY_PASSWORD, "pass1");
        data.put(ConfigConstants.PLUGIN_PREFIX, true);
        data.put(ConfigConstants.PLUGIN_NAME, "native-jna");

        NyxConfig config = NyxConfig.fromMap(data);

        assertEquals(9090, config.getServerPort());
        assertFalse(config.getIsServerOrClient());
        assertEquals("/custom/ws", config.getWsServerUrl());
        assertEquals("ws://custom:3001", config.getWsClientUrl());
        assertEquals("test-token", config.getToken());
        assertEquals("http://proxy:7890", config.getHttpProxy());
        assertEquals("socks5://proxy:7891", config.getSocksProxy());
        assertEquals("user1", config.getProxyUser());
        assertEquals("pass1", config.getProxyPassword());
        assertTrue(config.getPluginPrefix());
        assertEquals("native-jna", config.getPluginName());
    }

    @Test
    @DisplayName("fromMap — 空 Map 使用默认值")
    void testFromMapEmpty() {
        NyxConfig config = NyxConfig.fromMap(new HashMap<>());

        assertEquals(8080, config.getServerPort());
        assertTrue(config.getIsServerOrClient());
        assertEquals("/ws/shiro", config.getWsServerUrl());
        assertEquals("ws://localhost:3001", config.getWsClientUrl());
        assertEquals("", config.getToken());
        assertEquals("", config.getHttpProxy());
        assertEquals("", config.getSocksProxy());
        assertEquals("", config.getProxyUser());
        assertEquals("", config.getProxyPassword());
        assertFalse(config.getPluginPrefix());
        assertEquals("", config.getPluginName());
    }

    @Test
    @DisplayName("fromMap — null 值 Key 安全降级")
    void testFromMapNullValue() {
        Map<String, Object> data = new HashMap<>();
        data.put(ConfigConstants.SERVER_PORT, null);
        data.put(ConfigConstants.TOKEN, "valid");
        data.put(ConfigConstants.PLUGIN_NAME, null);

        NyxConfig config = NyxConfig.fromMap(data);

        assertEquals(8080, config.getServerPort());
        assertEquals("valid", config.getToken());
        assertEquals("", config.getPluginName());
    }

    @Test
    @DisplayName("fromMap — 类型不匹配时安全降级")
    void testFromMapTypeMismatch() {
        Map<String, Object> data = new HashMap<>();
        data.put(ConfigConstants.SERVER_PORT, "not-a-number");
        data.put(ConfigConstants.IS_SERVER_OR_CLIENT, "not-a-boolean");
        data.put(ConfigConstants.WS_SERVER_URL, 12345);

        NyxConfig config = NyxConfig.fromMap(data);

        assertEquals(8080, config.getServerPort());
        assertTrue(config.getIsServerOrClient());
        assertEquals("/ws/shiro", config.getWsServerUrl());
    }

    @Test
    @DisplayName("toMap — 全部字段正确序列化")
    void testToMap() {
        NyxConfig config = new NyxConfig();
        config.setServerPort(9090);
        config.setIsServerOrClient(false);
        config.setWsServerUrl("/test/ws");
        config.setWsClientUrl("ws://test:3001");
        config.setToken("t");
        config.setHttpProxy("http://p:7890");
        config.setSocksProxy("socks5://p:7891");
        config.setProxyUser("u");
        config.setProxyPassword("pw");
        config.setPluginPrefix(true);
        config.setPluginName("test-plugin");

        Map<String, Object> map = config.toMap();

        assertEquals(9090, map.get(ConfigConstants.SERVER_PORT));
        assertEquals(false, map.get(ConfigConstants.IS_SERVER_OR_CLIENT));
        assertEquals("/test/ws", map.get(ConfigConstants.WS_SERVER_URL));
        assertEquals("ws://test:3001", map.get(ConfigConstants.WS_CLIENT_URL));
        assertEquals("t", map.get(ConfigConstants.TOKEN));
        assertEquals("http://p:7890", map.get(ConfigConstants.HTTP_PROXY));
        assertEquals("socks5://p:7891", map.get(ConfigConstants.SOCKS_PROXY));
        assertEquals("u", map.get(ConfigConstants.PROXY_USER));
        assertEquals("pw", map.get(ConfigConstants.PROXY_PASSWORD));
        assertEquals(true, map.get(ConfigConstants.PLUGIN_PREFIX));
        assertEquals("test-plugin", map.get(ConfigConstants.PLUGIN_NAME));
    }

    @Test
    @DisplayName("toMap — 默认值正确序列化")
    void testToMapDefaults() {
        NyxConfig config = new NyxConfig();
        Map<String, Object> map = config.toMap();

        assertEquals(8080, map.get(ConfigConstants.SERVER_PORT));
        assertEquals(true, map.get(ConfigConstants.IS_SERVER_OR_CLIENT));
        assertEquals("/ws/shiro", map.get(ConfigConstants.WS_SERVER_URL));
        assertEquals("ws://localhost:3001", map.get(ConfigConstants.WS_CLIENT_URL));
        assertNull(map.get(ConfigConstants.TOKEN));
        assertNull(map.get(ConfigConstants.HTTP_PROXY));
        assertNull(map.get(ConfigConstants.SOCKS_PROXY));
        assertNull(map.get(ConfigConstants.PROXY_USER));
        assertNull(map.get(ConfigConstants.PROXY_PASSWORD));
        assertEquals(false, map.get(ConfigConstants.PLUGIN_PREFIX));
        assertNull(map.get(ConfigConstants.PLUGIN_NAME));
    }

    @Test
    @DisplayName("fromMap → toMap — 双向转换一致性")
    void testRoundTrip() {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put(ConfigConstants.SERVER_PORT, 8080);
        original.put(ConfigConstants.IS_SERVER_OR_CLIENT, true);
        original.put(ConfigConstants.WS_SERVER_URL, "/ws/shiro");
        original.put(ConfigConstants.WS_CLIENT_URL, "ws://localhost:3001");
        original.put(ConfigConstants.TOKEN, "test-token");
        original.put(ConfigConstants.HTTP_PROXY, "");
        original.put(ConfigConstants.SOCKS_PROXY, "");
        original.put(ConfigConstants.PROXY_USER, "");
        original.put(ConfigConstants.PROXY_PASSWORD, "");
        original.put(ConfigConstants.PLUGIN_PREFIX, false);
        original.put(ConfigConstants.PLUGIN_NAME, "");

        // fromMap → toMap 应保持字段值一致
        Map<String, Object> result = NyxConfig.fromMap(original).toMap();

        original.forEach((key, value) -> assertEquals(value, result.get(key), "Key: " + key));
    }

    @Test
    @DisplayName("mergeInto — 非 null 字段合并到目标 Map")
    void testMergeInto() {
        Map<String, Object> target = new HashMap<>();
        target.put(ConfigConstants.SERVER_PORT, 8080);
        target.put(ConfigConstants.PLUGIN_PREFIX, false);

        NyxConfig config = new NyxConfig();
        config.setServerPort(9090);            // 非 null → 覆盖
        config.setPluginPrefix(true);           // 非 null → 覆盖
        config.setPluginName("new-plugin");     // 非 null → 新增

        config.mergeInto(target);

        assertEquals(9090, target.get(ConfigConstants.SERVER_PORT));
        assertEquals(true, target.get(ConfigConstants.PLUGIN_PREFIX));
        assertEquals("new-plugin", target.get(ConfigConstants.PLUGIN_NAME));
    }

    @Test
    @DisplayName("mergeInto — null 字段不覆盖已有值")
    void testMergeIntoNullNotOverwrite() {
        Map<String, Object> target = new HashMap<>();
        target.put(ConfigConstants.SERVER_PORT, 8080);

        NyxConfig config = new NyxConfig();
        config.setServerPort(null);  // null → 不覆盖

        config.mergeInto(target);

        assertEquals(8080, target.get(ConfigConstants.SERVER_PORT));
    }

    @Test
    @DisplayName("mergeInto — 空目标 Map 正确新增字段")
    void testMergeIntoEmptyTarget() {
        NyxConfig config = new NyxConfig();
        config.setServerPort(9090);
        config.setPluginName("plugin");

        Map<String, Object> target = new HashMap<>();
        config.mergeInto(target);

        assertEquals(9090, target.get(ConfigConstants.SERVER_PORT));
        assertEquals("plugin", target.get(ConfigConstants.PLUGIN_NAME));
    }
}
