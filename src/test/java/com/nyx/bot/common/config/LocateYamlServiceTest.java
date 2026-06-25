package com.nyx.bot.common.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocateYamlService 文件读写和线程安全测试
 */
class LocateYamlServiceTest {

    private Path tempConfigPath;
    private Path originalConfigPath;
    private LocateYamlService service;

    @BeforeEach
    void setUp() throws Exception {
        // 保存原始路径，设置临时路径
        originalConfigPath = LocateYamlService.CONFIG_PATH;
        Path tempDir = Files.createTempDirectory("nyxbot-locate-test");
        tempConfigPath = tempDir.resolve("locate.yaml");
        Files.createDirectories(tempConfigPath.getParent());
        LocateYamlService.CONFIG_PATH = tempConfigPath;

        service = new LocateYamlService();
    }

    @AfterEach
    void tearDown() {
        // 恢复原始路径
        LocateYamlService.CONFIG_PATH = originalConfigPath;
        // 清理临时文件
        try {
            Files.deleteIfExists(tempConfigPath);
            Files.deleteIfExists(tempConfigPath.getParent());
        } catch (IOException ignored) {
        }
    }

    @Test
    @DisplayName("load — 文件不存在返回空配置")
    void testLoadFileNotExists() {
        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("save → load — 完整读写一致性")
    void testSaveAndLoad() {
        Map<String, Object> data = new HashMap<>();
        data.put("serverPort", 9090);
        data.put("token", "test-token");
        data.put("pluginName", "test-plugin");

        service.save(data);
        Map<String, Object> loaded = service.load();

        assertEquals(9090, loaded.get("serverPort"));
        assertEquals("test-token", loaded.get("token"));
        assertEquals("test-plugin", loaded.get("pluginName"));
        assertEquals(3, loaded.size());
    }

    @Test
    @DisplayName("save — 覆盖已有文件内容")
    void testSaveOverwrite() {
        Map<String, Object> first = new HashMap<>();
        first.put("serverPort", 8080);
        first.put("token", "old-token");
        service.save(first);

        Map<String, Object> second = new HashMap<>();
        second.put("serverPort", 9090);
        second.put("token", "new-token");
        service.save(second);

        Map<String, Object> loaded = service.load();
        assertEquals(9090, loaded.get("serverPort"));
        assertEquals("new-token", loaded.get("token"));
        assertEquals(2, loaded.size());
    }

    @Test
    @DisplayName("update — 读-改-写完整性")
    void testUpdate() {
        service.save(new HashMap<>());

        service.update(data -> {
            data.put("serverPort", 8080);
            data.put("token", "from-update");
        });

        Map<String, Object> loaded = service.load();
        assertEquals(8080, loaded.get("serverPort"));
        assertEquals("from-update", loaded.get("token"));
    }

    @Test
    @DisplayName("update — 增量更新保留已有字段")
    void testUpdateIncremental() {
        Map<String, Object> initial = new HashMap<>();
        initial.put("serverPort", 8080);
        initial.put("token", "original");
        service.save(initial);

        service.update(data -> data.put("token", "updated"));

        Map<String, Object> loaded = service.load();
        assertEquals(8080, loaded.get("serverPort"));
        assertEquals("updated", loaded.get("token"));
    }

    @Test
    @DisplayName("load — 损坏的 YAML 内容回退空配置")
    void testLoadCorruptedYaml() {
        try {
            Files.writeString(tempConfigPath, "{ broken yaml : unclosed");
        } catch (IOException e) {
            fail("无法写入测试文件: " + e.getMessage());
        }

        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("load — 非 Map 根节点回退空配置")
    void testLoadNonMapRoot() {
        try {
            Files.writeString(tempConfigPath, "just-a-string");
        } catch (IOException e) {
            fail("无法写入测试文件: " + e.getMessage());
        }

        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("load — 数组根节点回退空配置")
    void testLoadArrayRoot() {
        try {
            Files.writeString(tempConfigPath, "- item1\n- item2");
        } catch (IOException e) {
            fail("无法写入测试文件: " + e.getMessage());
        }

        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("并发 save 不抛异常且文件可读")
    void testConcurrentSave() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            Thread.ofVirtual().start(() -> {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("index", idx);
                    data.put("value", "thread-" + idx);
                    service.save(data);
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertNull(error.get(), "并发 save 不应抛异常");

        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue(result.containsKey("index"));
        assertTrue(result.containsKey("value"));
    }

    @Test
    @DisplayName("并发 update 通过锁保证数据完整")
    void testConcurrentUpdate() throws InterruptedException {
        Map<String, Object> initial = new HashMap<>();
        initial.put("counter", 0);
        service.save(initial);

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    service.update(data -> {
                        Integer counter = (Integer) data.getOrDefault("counter", 0);
                        data.put("counter", counter + 1);
                    });
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertNull(error.get(), "并发 update 不应抛异常");

        Map<String, Object> result = service.load();
        assertNotNull(result);
        assertTrue((Integer) result.getOrDefault("counter", 0) >= 1);
    }
}
