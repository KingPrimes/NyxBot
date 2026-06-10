package com.nyx.bot.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.FileUtils;
import io.github.kingprimes.model.Arbitration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_GLOBAL_STATES_ARBITRATION;

@Slf4j
@Component
public class ArbitrationCache {

    private static final String ARBITRATION_FILE = "./data/arbitration";
    private static final long SEVEN_DAYS_SECONDS = TimeUnit.DAYS.toSeconds(7);
    private static final TypeReference<List<Arbitration>> LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ArbitrationCache(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ══════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════

    private static boolean hasValidData(List<Arbitration> list) {
        long now = Instant.now().getEpochSecond();
        return list.stream().anyMatch(ar -> ar.getExpiry().getEpochSecond() > now);
    }

    /**
     * 裁剪：仅保留 expiry > now 且在7天内的数据
     */
    private static List<Arbitration> pruneToRecent(List<Arbitration> full) {
        long now = Instant.now().getEpochSecond();
        long cutoff = now + SEVEN_DAYS_SECONDS;
        return full.stream()
                .filter(ar -> ar.getExpiry().getEpochSecond() > now)
                .filter(ar -> ar.getActivation().getEpochSecond() < cutoff)
                .toList();
    }

    private static List<Arbitration> getFromMemoryCache() {
        List<?> data = CacheUtils.get(WARFRAME_GLOBAL_STATES_ARBITRATION, "data", List.class);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return data.stream()
                .filter(d -> d instanceof Arbitration)
                .map(d -> (Arbitration) d)
                .toList();
    }

    private static void setToMemoryCache(List<Arbitration> list) {
        CacheUtils.set(WARFRAME_GLOBAL_STATES_ARBITRATION, "data", list);
    }

    /**
     * 初始化仲裁缓存（启动时调用）
     * 优先从本地文件恢复，文件不存在或全部过期则从API获取
     */
    public void init() {
        List<Arbitration> fromFile = loadFromFile();
        if (fromFile != null && !fromFile.isEmpty() && hasValidData(fromFile)) {
            List<Arbitration> pruned = pruneToRecent(fromFile);
            setToMemoryCache(pruned);
            log.info("从文件初始化仲裁数据，裁剪后{}条", pruned.size());
        } else {
            fetchAndCache();
        }
    }

    // ══════════════════════════════════════════════
    // 三级回退加载：内存缓存 → 文件恢复 → API获取
    // ══════════════════════════════════════════════

    /**
     * 强制从远程API重新获取仲裁数据
     */
    public void reloadArbitration() {
        fetchAndCache();
    }

    // ══════════════════════════════════════════════
    // 数据验证与裁剪
    // ══════════════════════════════════════════════

    /**
     * 获取当前正在进行的仲裁任务
     */
    public Optional<Arbitration> getArbitration() {
        List<Arbitration> list = loadArbitrationList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        long now = Instant.now().getEpochSecond();
        return list.stream()
                .filter(ar -> ar.getExpiry().getEpochSecond() > now)
                .min(Comparator.comparingLong(ar -> ar.getExpiry().getEpochSecond() - now));
    }

    /**
     * 从外部设置仲裁数据（启动时从文件恢复调用）
     * 仅裁剪后写入内存缓存，不做文件持久化
     */
    public void setArbitration(List<?> rawList) {
        List<Arbitration> full = rawList.stream()
                .filter(d -> d instanceof Arbitration)
                .map(d -> (Arbitration) d)
                .toList();
        if (!full.isEmpty()) {
            List<Arbitration> pruned = pruneToRecent(full);
            setToMemoryCache(pruned);
            log.info("仲裁数据已从外部设置，裁剪后内存缓存{}条", pruned.size());
        }
    }

    // ══════════════════════════════════════════════
    // 内存缓存
    // ══════════════════════════════════════════════

    /**
     * 获取有价值的未来仲裁任务列表（前瞻）
     */
    public List<Arbitration> getArbitrationList() {
        long now = Instant.now().getEpochSecond();
        return loadArbitrationList().stream()
                .filter(Arbitration::isWorth)
                .filter(ar -> ar.getActivation().getEpochSecond() > now)
                .limit(10)
                .toList();
    }

    private List<Arbitration> loadArbitrationList() {
        List<Arbitration> cached = getFromMemoryCache();
        if (cached != null && hasValidData(cached)) {
            return cached;
        }

        List<Arbitration> fromFile = loadFromFile();
        if (fromFile != null && !fromFile.isEmpty()) {
            if (hasValidData(fromFile)) {
                List<Arbitration> pruned = pruneToRecent(fromFile);
                setToMemoryCache(pruned);
                log.info("从文件恢复仲裁数据，裁剪后{}条", pruned.size());
                return pruned;
            }
            log.info("文件中的仲裁数据已全部过期，将从API获取");
        }

        return fetchAndCache();
    }

    // ══════════════════════════════════════════════
    // 文件持久化
    // ══════════════════════════════════════════════

    private List<Arbitration> loadFromFile() {
        try {
            String base64 = FileUtils.readFileToString(ARBITRATION_FILE);
            if (base64.isEmpty()) {
                return null;
            }
            byte[] bytes = Base64.getDecoder().decode(base64);
            return objectMapper.readValue(bytes, LIST_TYPE);
        } catch (Exception e) {
            log.warn("从文件读取仲裁数据失败: {}", e.getMessage());
            return null;
        }
    }

    private void persistToFile(List<Arbitration> list) {
        try {
            String base64 = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(list));
            FileUtils.writeFile(ARBITRATION_FILE, base64.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("持久化仲裁数据失败: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════
    // API获取
    // ══════════════════════════════════════════════

    /**
     * 从远程API获取仲裁数据，完整数据持久化到文件，裁剪后存入内存缓存
     */
    private List<Arbitration> fetchAndCache() {
        List<Arbitration> full = ApiUrl.arbitrationPreList();
        if (full.isEmpty()) {
            log.warn("API返回的仲裁数据为空");
            return List.of();
        }
        persistToFile(full);
        List<Arbitration> pruned = pruneToRecent(full);
        setToMemoryCache(pruned);
        log.info("从API获取仲裁数据，完整{}条，裁剪后内存缓存{}条", full.size(), pruned.size());
        return pruned;
    }
}
