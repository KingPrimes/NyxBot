package com.nyx.bot.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * locate.yaml 文件读写服务。
 * <p>
 * 职责仅限于 YAML 序列化/反序列化，不参与优先级解析。
 * 使用 try-with-resources 确保资源正确关闭，避免资源泄漏。
 * </p>
 *
 * @author KingPrimes
 */
@Component
public class LocateYamlService {

    private static final Logger log = LoggerFactory.getLogger(LocateYamlService.class);
    private static final Path CONFIG_PATH = Path.of("./data/locate.yaml");

    private final Yaml yaml;

    public LocateYamlService() {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(opts);
    }

    /**
     * 从 YAML 文件读取配置，文件不存在时返回空配置。
     */
    public Map<String, Object> load() {
        if (!Files.exists(CONFIG_PATH)) {
            return new LinkedHashMap<>();
        }
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Map<String, Object> result = yaml.load(in);
            return result != null ? result : new LinkedHashMap<>();
        } catch (IOException e) {
            log.warn("读取 locate.yaml 失败，使用空配置: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    /**
     * 将配置持久化到 YAML 文件。
     */
    public void save(Map<String, Object> data) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String yamlStr = yaml.dumpAs(data, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
            Files.writeString(CONFIG_PATH, yamlStr, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("写入 locate.yaml 失败: {}", e.getMessage(), e);
            throw new UncheckedIOException("无法持久化配置到 locate.yaml", e);
        }
    }
}
