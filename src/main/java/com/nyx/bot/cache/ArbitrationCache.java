package com.nyx.bot.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.utils.CachePersistenceUtils;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.model.Arbitration;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_GLOBAL_STATES_ARBITRATION;

@Slf4j
public class ArbitrationCache {

    /**
     * 重新从远程API获取并缓存仲裁数据
     */
    public static void reloadArbitration() {
        fetchAndCacheArbitrationList();
    }

    /**
     * 获取当前有价值的仲裁任务列表
     * <p>过滤条件：任务标记为有价值(isWorth=true)且尚未过期</p>
     *
     * @return 符合条件的仲裁任务列表，最多10条
     */
    public static List<Arbitration> getArbitrationList() {
        long currentMilli = Instant.now().getEpochSecond();

        List<Arbitration> result = loadArbitrationList().stream()
                .filter(Arbitration::isWorth)
                .filter(ar -> ar.getActivation().getEpochSecond() > currentMilli)
                .limit(10)
                .toList();

        if (result.isEmpty()) {
            log.info("仲裁列表为空，开始重载仲裁数据");
            reloadArbitration();
            result = loadArbitrationList().stream()
                    .filter(Arbitration::isWorth)
                    .filter(ar -> ar.getActivation().getEpochSecond() > currentMilli)
                    .limit(10)
                    .toList();
            log.info("重载后获取到 {} 个符合条件的仲裁", result.size());
        }

        return result;
    }

    /**
     * 从缓存中加载仲裁列表，缓存为空时自动从远程获取
     *
     * @return 仲裁数据列表
     */
    private static List<Arbitration> loadArbitrationList() {
        List<?> data = CacheUtils.get(WARFRAME_GLOBAL_STATES_ARBITRATION, "data", List.class);

        if (data == null || data.isEmpty()) {
            return fetchAndCacheArbitrationList();
        }

        return data.stream()
                .filter(d -> d instanceof Arbitration)
                .map(d -> (Arbitration) d)
                .toList();
    }

    /**
     * 从远程API获取仲裁列表并写入缓存
     *
     * @return 远程获取的仲裁数据列表
     */
    private static List<Arbitration> fetchAndCacheArbitrationList() {
        List<Arbitration> arbitrationList = ApiUrl.arbitrationPreList();
        if (!arbitrationList.isEmpty()) {
            setArbitration(arbitrationList);
        }
        return arbitrationList;
    }

    /**
     * 获取当前正在进行的仲裁任务
     * <p>从仲裁列表中选择过期时间最早且尚未过期的任务</p>
     *
     * @return 当前仲裁任务，无有效任务时返回 Optional.empty()
     */
    public static Optional<Arbitration> getArbitration() {
        List<Arbitration> arbitrationList = loadArbitrationList();
        if (arbitrationList.isEmpty()) {
            return Optional.empty();
        }
        long milli = Instant.now().getEpochSecond();
        Arbitration a = arbitrationList.stream()
                .filter(ar -> ar.getExpiry().getEpochSecond() - milli > 0)
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getEpochSecond() - milli))
                .orElse(null);
        if (a == null) {
            fetchAndCacheArbitrationList();
            return Optional.empty();
        }

        return Optional.of(a);
    }

    /**
     * 设置仲裁列表数据并持久化到本地文件
     * <p>将数据写入缓存(15天TTL)并以Base64编码持久化到本地，用于重启后恢复</p>
     *
     * @param arbitrationList 仲裁数据列表
     */
    public static void setArbitration(List<Arbitration> arbitrationList) {
        try {
            ObjectMapper mapper = SpringUtils.getBean(ObjectMapper.class);
            String base64 = Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(arbitrationList));
            CachePersistenceUtils.setAndPersist(
                    WARFRAME_GLOBAL_STATES_ARBITRATION, arbitrationList, 15L, TimeUnit.DAYS,
                    "./data/arbitration", base64, "Arbitration");
        } catch (Exception e) {
            log.error("序列化Arbitration失败: {}", e.getMessage());
        }
    }
}
