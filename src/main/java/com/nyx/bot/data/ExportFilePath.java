package com.nyx.bot.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpFileDownloader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 导出文件管理工具 — 路径解析、下载、哈希对比、缓存检查
 *
 * @author KingPrimes
 */
@Slf4j
public final class ExportFilePath {

    /**
     * 导出文件目录格式
     */
    static final String EXPORT_PATH_FORMAT = "./data/export/%s";
    /**
     * keys.json 路径
     */
    static final String KEYS_PATH = "./data/keys.json";
    /**
     * LZMA 索引下载路径
     */
    private static final String LAMA_PATH = "./data/lzma/index_zh.txt.lzma";
    /**
     * LZMA 解压输出路径
     */
    private static final String INDEX_PATH = "./data/lzma/index_zh.txt";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ReentrantLock KEY_MAP_LOCK = new ReentrantLock();
    /**
     * keys.json 前缀→完整文件名映射，首次使用时加载并缓存
     */
    private static volatile Map<String, String> keyMap;

    private ExportFilePath() {
    }

    // ══════════════════════════════════════════════
    // 路径解析
    // ══════════════════════════════════════════════

    /**
     * 根据前缀解析导出文件完整路径。
     * 优先使用 keys.json 中的实际文件名映射，未找到时回退到 {prefix}_zh.json 规则。
     *
     * @param prefix 文件前缀，如 "ExportWeapons"
     * @return 完整路径，如 "./data/export/ExportWeapons_zh.json"
     */
    public static String resolve(String prefix) {
        Map<String, String> map = getKeyMap();
        String filename = map.getOrDefault(prefix, prefix + "_zh.json");
        return EXPORT_PATH_FORMAT.formatted(filename);
    }

    /**
     * 检查导出文件本地缓存是否存在
     */
    public static boolean localCacheExists() {
        File exportDir = new File("./data/export");
        if (!exportDir.exists() || !exportDir.isDirectory()) {
            return false;
        }
        String[] files = exportDir.list();
        return files != null && files.length > 0;
    }

    // ══════════════════════════════════════════════
    // 文件下载
    // ══════════════════════════════════════════════

    /**
     * 下载 DE 官方导出数据文件（使用默认中文路径）
     */
    public static Boolean severExportFiles() {
        return severExportFiles("zh", LAMA_PATH, INDEX_PATH, EXPORT_PATH_FORMAT);
    }

    /**
     * 获取DE官方数据文件并保存到本地
     *
     * @param languagesKey 语言
     * @param path         lzma 文件保存路径
     * @param outPath      解压文件保存路径
     * @param exportPath   索引数据保存路径 ./data/export/%s
     */
    static Boolean severExportFiles(String languagesKey, String path, String outPath, String exportPath) {
        Boolean files = getExportLZMAFiles(languagesKey, path);
        if (!files) {
            return false;
        }
        boolean falg = true;
        if (ZipUtils.unLzma(path, outPath)) {
            List<String> keys = FileUtils.readFileToList(outPath);
            Map<String, String> compared = compareTheHashAndSave(keys);
            if (compared.isEmpty()) {
                log.info("Lzma 数据无变化，无需获取更新！");
                return true;
            }
            for (String key : keys) {
                if (key.contains("ExportRecipes") || key.contains("ExportFusionBundles")) {
                    continue;
                }
                falg = getExportFiles(key, exportPath.formatted(StringUtils.substring(key, 0, key.indexOf("!"))));
            }
        }
        return falg;
    }

    /**
     * 获取LZMA 索引文件
     */
    private static Boolean getExportLZMAFiles(String key, String path) {
        return HttpFileDownloader.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted(key), path);
    }

    /**
     * 根据索引获取数据
     */
    private static Boolean getExportFiles(String key, String path) {
        log.debug("ExportFiles URL:{} Path:{}", key, path);
        return HttpFileDownloader.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_MANIFESTS.formatted(key), path);
    }

    // ══════════════════════════════════════════════
    // 哈希对比
    // ══════════════════════════════════════════════

    /**
     * 对比Lzma数据的Hash值并保存，返回不同的Hash值
     */
    private static Map<String, String> compareTheHashAndSave(List<String> keys) {
        log.info("开始对比 Lzma 数据的 Hash 值并保存！");
        String keysHashPath = KEYS_PATH;
        Map<String, String> collect = keys.stream()
                .map(key -> key.split("!", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
        try {
            Map<String, String> map = MAPPER.readValue(
                    new FileInputStream(keysHashPath),
                    new TypeReference<Map<String, String>>() {
                    });
            FileUtils.writeFile(keysHashPath, MAPPER.writeValueAsString(collect));
            return collect.entrySet().stream()
                    .filter(e -> !Objects.equals(e.getValue(), map.get(e.getKey())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (FileNotFoundException e) {
            try {
                FileUtils.writeFile(keysHashPath, MAPPER.writeValueAsString(collect));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to write keys file", ex);
            }
            return collect;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse keys file", e);
        }
    }

    // ══════════════════════════════════════════════
    // keys.json 映射加载
    // ══════════════════════════════════════════════

    /**
     * 获取 keys.json 的 前缀→完整文件名 映射（DCL 懒加载）
     */
    private static Map<String, String> getKeyMap() {
        if (keyMap != null) {
            return keyMap;
        }
        KEY_MAP_LOCK.lock();
        try {
            if (keyMap != null) {
                return keyMap;
            }
            File file = new File(KEYS_PATH);
            if (!file.exists()) {
                log.debug("keys.json 不存在，使用默认命名规则");
                keyMap = Map.of();
                return keyMap;
            }
            try {
                Map<String, String> raw = MAPPER.readValue(file, new TypeReference<>() {
                });
                Map<String, String> map = new HashMap<>();
                for (String fullName : raw.keySet()) {
                    int idx = fullName.indexOf('_');
                    String prefix = idx > 0 ? fullName.substring(0, idx) : fullName;
                    map.putIfAbsent(prefix, fullName);
                }
                keyMap = map;
                log.debug("已加载 {} 个导出文件映射", keyMap.size());
            } catch (Exception e) {
                log.error("读取 keys.json 失败: {}", e.getMessage());
                keyMap = Map.of();
            }
            return keyMap;
        } finally {
            KEY_MAP_LOCK.unlock();
        }
    }
}
