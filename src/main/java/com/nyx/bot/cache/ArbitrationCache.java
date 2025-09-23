package com.nyx.bot.cache;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.res.Arbitration;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_GLOBAL_STATES_ARBITRATION;

@Slf4j
public class ArbitrationCache {
    /**
     * 重载仲裁数据
     */
    public static void reloadArbitration() {
        fetchAndCacheArbitrationList();
    }

    /**
     * 获取有价值的仲裁列表
     *
     * @return List<ArbitrationPlugin>
     */
    public static List<Arbitration> getArbitrationList() {
        return loadArbitrationList().stream().filter(Arbitration::isWorth).limit(10).peek(a -> a.setEtc(DateUtils.getDiff((a.getExpiry()), new Date(), true))).toList();
    }

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

    private static List<Arbitration> fetchAndCacheArbitrationList() {
        List<Arbitration> arbitrationList = ApiUrl.arbitrationPreList();
        if (!arbitrationList.isEmpty()) {
            setArbitration(arbitrationList);
        }
        return arbitrationList;
    }

    /**
     * 获取当前的仲裁信息
     *
     * @return 当前数据
     */
    public static Optional<Arbitration> getArbitration() {
        List<Arbitration> arbitrationList = loadArbitrationList();
        if (arbitrationList.isEmpty()) {
            return Optional.empty();
        }
        long milli = ZonedDateTime.of(LocalDateTime.now(ZoneOffset.ofHours(8)), ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        Arbitration a = arbitrationList.stream()
                //过滤掉过期的数据
                .filter(ar -> ar.getExpiry().getTime() - milli > 0)
                //判断两个时间相差的毫秒数，并取最小值的元素
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getTime() - milli))
                .stream().peek(ar -> {
                    ar.setEtc(DateUtils.getDiff((ar.getExpiry()), new Date(), true));
                    ar.setEnemy(ar.getEnemy().replace("Infestation", "Infested"));
                })
                .findFirst()
                .orElse(null);
        if (a == null) {
            fetchAndCacheArbitrationList();
            return Optional.empty();
        }

        return Optional.of(a);
    }

    /**
     * 设置仲裁列表信息
     */
    public static void setArbitration(List<Arbitration> arbitrationList) {
        CacheUtils.set(WARFRAME_GLOBAL_STATES_ARBITRATION, "data", arbitrationList, 15L, TimeUnit.DAYS);
        FileUtils.writeFile("./data/arbitration", Base64.getEncoder().encodeToString(JSON.toJSONBytes(arbitrationList)));
    }
}
