package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.extra.spring.SpringCache2kCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheUtils {
    public static final String SYSTEM = "system";
    public static final String WARFRAME_SOCKET_DATA = "warframe-socket-data";
    public static final String WARFRAME_GLOBAL_STATES = "global-states";

    public static final String WARFRAME = "warframe";

    public static final String WARFRAME_GLOBAL_STATES_ARBITRATION = "global-states-arbitration";

    private static final CacheManager cm = SpringUtils.getBean(CacheManager.class);

    public static GlobalStates getGlobalState() throws DataNotInfoException {
        GlobalStates data = cm.getCache(WARFRAME_SOCKET_DATA).get("data", GlobalStates.class);
        if (data == null) {
            throw new DataNotInfoException(I18nUtils.message("error.warframe.data.null"));
        }
        return data;
    }

    public static void setGlobalState(GlobalStates state) {
        Objects.requireNonNull(cm.getCache(WARFRAME_SOCKET_DATA)).put("data", state);
        FileUtils.writeFile("./data/status", JSON.toJSONBytes(state));
    }

    public static void setArbitration(List<ArbitrationPre> arbitrationList) {
        cm.getCache(WARFRAME_GLOBAL_STATES_ARBITRATION).put("data", arbitrationList);
        FileUtils.writeFile("./data/arbitration", Base64.getEncoder().encodeToString(JSON.toJSONBytes(arbitrationList)));
    }

    /**
     * 获取有价值的仲裁列表
     *
     * @return List<ArbitrationPre>
     */
    @SuppressWarnings("unchecked")
    public static List<ArbitrationPre> getArbitrationList(String key) {
        Cache cache = cm.getCache(WARFRAME_GLOBAL_STATES_ARBITRATION);
        if (cache == null) {
            List<ArbitrationPre> arbitrationPres = ApiUrl.arbitrationPreList(key);
            if (!arbitrationPres.isEmpty()) {
                setArbitration(arbitrationPres);
            }
            return arbitrationPres;
        }

        // 类型安全转换
        List<ArbitrationPre> arbitrationList = (List<ArbitrationPre>) cache.get("data", List.class);
        if (arbitrationList.isEmpty()) {
            if (key != null && !key.isEmpty()) {
                arbitrationList = ApiUrl.arbitrationPreList(key);
                if (!arbitrationList.isEmpty()) {
                    setArbitration(arbitrationList);
                }
            }
        }
        return arbitrationList;
    }

    /**
     * 获取仲裁
     *
     * @return 当前数据
     */
    public static Optional<GlobalStates.Arbitration> getArbitration(String key) {
        List<ArbitrationPre> arbitrationList = getArbitrationList(key);
        if (arbitrationList.isEmpty()) {
            return Optional.empty();
        }
        GlobalStates.Arbitration arbitration = new GlobalStates.Arbitration();
        long milli = ZonedDateTime.of(LocalDateTime.now(ZoneOffset.ofHours(8)), ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        ArbitrationPre a = arbitrationList.stream()
                //过滤掉过期的数据
                .filter(ar -> ar.getExpiry().getTime() - milli > 0)
                //判断两个时间相差的毫秒数，并取最小值的元素
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getTime() - milli))
                .orElse(null);
        if (a == null) {
            setArbitration(ApiUrl.arbitrationPreList(key));
            return Optional.empty();
        }
        arbitration.setId(a.getId());
        arbitration.setActivation(a.getActivation());
        arbitration.setExpiry(a.getExpiry());
        arbitration.setNode(a.getNode());
        arbitration.setType(a.getType());
        arbitration.setEnemy(a.getEnemy());

        return Optional.of(arbitration);
    }


    /**
     * 设置缓存
     *
     * @param name 缓存名称
     * @param map  map k,v
     */
    public static void set(String name, Map<Object, Object> map) {
        map.forEach((k, v) -> Objects.requireNonNull(cm.getCache(name)).put(k, v));
    }

    /**
     * 设置缓存
     *
     * @param name 缓存名称
     * @param kv   KeyAndValue key,value
     */
    public static void set(String name, Object... kv) {
        //动态参数缺少数据
        if (kv.length % 2 != 0) {
            log.error("键值对缺少！");
            return;
        }
        //遍历动态参数
        for (int i = 0; i < kv.length + 1; ) {
            if (i + 1 < kv.length + 1) {
                Objects.requireNonNull(cm.getCache(name)).put(kv[i], kv[i + 1]);
            }
            if (i + 2 < kv.length + 1) {
                i += 2;
            } else {
                i++;
            }
        }
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @param key  key
     * @return Object
     */
    public static Object get(String name, Object key) {
        return cm.getCache(name).get(key).get();
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @param key  key
     * @param type 缓存的类
     * @return type参数的类
     */
    public static <T> T get(String name, Object key, @NotNull Class<T> type) {
        return cm.getCache(name).get(key, type);
    }

    public static boolean exists(String name, Object key) {
        return cm.getCache(name).get(key) != null;
    }

    /**
     * 存入缓存并指定过期时间（动态设置）
     *
     * @param cacheName 缓存名称
     * @param key       键
     * @param value     值
     * @param duration  时间长度
     * @param unit      时间单位
     */
    public static void putWithExpiry(String cacheName, Object key, Object value, long duration, TimeUnit unit) {
        Cache springCache = cm.getCache(cacheName);
        if (springCache instanceof SpringCache2kCache) {
            // 获取 Cache2k 原生缓存实例
            org.cache2k.Cache<Object, Object> nativeCache = ((SpringCache2kCache) springCache).getNativeCache();
            long expiryTime = System.currentTimeMillis() + unit.toMillis(duration);
            // 通过 EntryProcessor 设置值和过期时间
            nativeCache.invoke(key, entry -> {
                entry.setValue(value);
                entry.setExpiryTime(expiryTime);
                return entry;
            });
        } else {
            // 非 Cache2k 缓存实现时的回退逻辑
            springCache.put(key, value);
        }
    }
}
