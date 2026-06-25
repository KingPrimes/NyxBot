package com.nyx.bot.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * locate.yaml 文件读写服务。
 * <p>
 * 职责仅限于 YAML 序列化/反序列化，不参与优先级解析。
 * 使用 try-with-resources 确保资源正确关闭，避免资源泄漏。
 * </p>
 * <p>
 * <b>线程安全：</b>{@link #load()} 无锁可并发读，{@link #update(Consumer)} 和 {@link #save(Map)}
 * 使用 {@link ReentrantLock} 互斥写入，避免 {@code synchronized} 在虚拟线程下 pin 平台线程。
 * </p>
 *
 * @author KingPrimes
 */
@Component
public class LocateYamlService {

    private static final Logger log = LoggerFactory.getLogger(LocateYamlService.class);
    static Path CONFIG_PATH = Path.of("./data/locate.yaml");

    private final DumperOptions opts;
    private final ReentrantLock writeLock = new ReentrantLock();

    public LocateYamlService() {
        this.opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    /**
     * 从 YAML 文件读取配置，文件不存在时返回空配置。
     */
    public Map<String, Object> load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new LinkedHashMap<>();
        }
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Yaml yaml = new Yaml(opts);
            Object parsed = yaml.load(in);
            if (parsed instanceof Map<?, ?> m) {
                return (Map<String, Object>) m;
            }
            return new LinkedHashMap<>();
        } catch (IOException | YAMLException e) {
            log.warn("读取 locate.yaml 失败，使用空配置: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    /**
     * 原子更新配置：读 → 修改 → 写。
     * <p>
     * 在写锁内完成读-改-写，避免并发丢失更新。
     * </p>
     */
    public void update(Consumer<Map<String, Object>> updater) {
        writeLock.lock();
        try {
            Map<String, Object> data = load();
            updater.accept(data);
            doSave(data);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 将配置持久化到 YAML 文件。
     */
    public void save(Map<String, Object> data) {
        writeLock.lock();
        try {
            doSave(data);
        } finally {
            writeLock.unlock();
        }
    }

    private void doSave(Map<String, Object> data) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Yaml yaml = new Yaml(opts);
            String yamlStr = yaml.dumpAs(data, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
            Files.writeString(CONFIG_PATH, yamlStr, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("写入 locate.yaml 失败: {}", e.getMessage(), e);
            throw new UncheckedIOException("无法持久化配置到 locate.yaml", e);
        }
    }
}
