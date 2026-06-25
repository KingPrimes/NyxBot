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
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * locate.yaml 文件读写服务。
 * <p>
 * 职责仅限于 YAML 序列化/反序列化，不参与优先级解析。
 * 使用 try-with-resources 确保资源正确关闭，避免资源泄漏。
 * </p>
 * <p>
 * <b>线程安全：</b>无需加锁。采用原子文件模式——写入先写临时文件再 {@link Files#move}
 * 原子替换，读取始终看到完整内容。读写完全并行，不 pin 虚拟线程。
 * </p>
 *
 * @author KingPrimes
 */
@Component
public class LocateYamlService {

    private static final Logger log = LoggerFactory.getLogger(LocateYamlService.class);
    private static final Path CONFIG_PATH = Path.of("./data/locate.yaml");
    private static final Path TMP_PATH = Path.of("./data/locate.yaml.tmp");

    private final DumperOptions opts;

    public LocateYamlService() {
        this.opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    /**
     * 从 YAML 文件读取配置，文件不存在时返回空配置。
     * <p>
     * 由于写入使用原子重命名，此方法永远读到完整的文件内容。
     * </p>
     */
    public Map<String, Object> load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new LinkedHashMap<>();
        }
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Yaml yaml = new Yaml(opts);
            Object parsed = yaml.load(in);
            // 根节点必须是 Map，否则回退到空配置（防止 String/List/Integer 等类型触发 ClassCastException）
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
     * 非严格原子——高并发写入时"最后写入者胜出"。配置修改由用户手动触发，
     * 并发频率极低，此语义足够安全且无需锁。
     * </p>
     *
     * @param updater 修改回调，接收当前配置 Map
     */
    public void update(Consumer<Map<String, Object>> updater) {
        Map<String, Object> data = load();
        updater.accept(data);
        save(data);
    }

    /**
     * 将配置持久化到 YAML 文件（原子写入）。
     * <p>
     * 先写入 {@code locate.yaml.tmp}，再原子重命名为 {@code locate.yaml}，
     * 确保读取时永远不会读到半写文件。
     * </p>
     */
    public void save(Map<String, Object> data) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Yaml yaml = new Yaml(opts);
            String yamlStr = yaml.dumpAs(data, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
            // 先写临时文件
            Files.writeString(TMP_PATH, yamlStr, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            // 原子替换：读进程永远看不到半写文件
            Files.move(TMP_PATH, CONFIG_PATH, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("写入 locate.yaml 失败: {}", e.getMessage(), e);
            throw new UncheckedIOException("无法持久化配置到 locate.yaml", e);
        }
    }
}
