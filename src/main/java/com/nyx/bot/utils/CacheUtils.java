package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CacheUtils {
    public static final String SYSTEM = "system";
    public static final String WARFRAME_SOCKET_DATA = "warframe-socket-data";
    public static final String GROUP_CAPTCHA = "group-captcha";
    public static final String WARFRAME_GLOBAL_STATES = "global-states";

    public static final String USER = "user";

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

    public static void setUser(String token, SysUser user) {
        set(USER, token, user);
    }

    public static SysUser getUser(String token) {
        return get(USER, token, SysUser.class);
    }

    public static void delUser(String token) {
        Objects.requireNonNull(cm.getCache(USER)).evict(token);
    }

    /**
     * 获取仲裁
     *
     * @return 当前数据
     */
    public static GlobalStates.Arbitration getArbitration() {
        List<?> arbitrationList = Objects.requireNonNull(cm.getCache(WARFRAME_GLOBAL_STATES_ARBITRATION)).get("arbitration", List.class);
        if (arbitrationList == null || arbitrationList.isEmpty()) {
            arbitrationList = ApiUrl.arbitrationPreList();
            Objects.requireNonNull(cm.getCache(WARFRAME_GLOBAL_STATES_ARBITRATION)).put("arbitration", arbitrationList);
        }
        AtomicReference<GlobalStates.Arbitration> arbitration = new AtomicReference<>(new GlobalStates.Arbitration());
        long milli = ZonedDateTime.of(LocalDateTime.now(ZoneOffset.ofHours(8)), ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
        arbitrationList.stream()
                // 类型转换
                .filter(i -> i instanceof ArbitrationPre)
                .map(i -> (ArbitrationPre) i)
                //过滤掉过期的数据
                .filter(a -> a.getExpiry().getTime() - milli > 0)
                //判断两个时间相差的毫秒数，并取最小值的元素
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getTime() - milli))
                // 赋值
                .ifPresentOrElse(a -> {
                    arbitration.get().setActivation(a.getActivation());
                    arbitration.get().setExpiry(a.getExpiry());
                    arbitration.get().setNode(a.getNode());
                    arbitration.get().setType(a.getType());
                    arbitration.get().setEnemy(a.getEnemy());
                }, () -> {
                    // 设置为空，说明缓存中没有匹配的值了
                    arbitration.set(null);
                });
        // 如何没有匹配的值则获取新的数据
        if (arbitration.get() == null) {
            Objects.requireNonNull(cm.getCache(WARFRAME_GLOBAL_STATES_ARBITRATION)).put("arbitration", ApiUrl.arbitrationPreList());
            // 迭代返回数据
            return getArbitration();
        }
        return arbitration.get();
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
}
