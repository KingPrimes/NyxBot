package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.annotation.Nullable;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CacheUtils {
    public static final String SYSTEM = "system";
    public static final String WARFRAME_SOCKET_DATA = "warframe-socket-data";
    public static final String GROUP_CAPTCHA = "group-captcha";
    private static final CacheManager cm = SpringUtils.getBean(CacheManager.class);

    public static GlobalStates getGlobalState() throws DataNotInfoException {
        GlobalStates data = Objects.requireNonNull(cm.getCache(WARFRAME_SOCKET_DATA)).get("data", GlobalStates.class);
        if (data == null) {
            throw new DataNotInfoException(I18nUtils.message("error.warframe.data.null"));
        }
        return data;
    }

    public static void setGlobalState(GlobalStates state) {
        if (state.getArbitration() == null){
            ResourceUtil resourceUtil = new ResourceUtil();
            String arbitrations = resourceUtil.getArbitrations();
            if (arbitrations != null) {
                List<ArbitrationPre> arbitrationPres = JSONArray.parseArray(arbitrations, ArbitrationPre.class, JSONReader.Feature.SupportSmartMatch);
                LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
                String format = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));
                ArbitrationPre arbitrationPre = arbitrationPres.stream().filter(i -> DateUtils.format(i.getActivation(),"yyyy-MM-dd HH:mm:ss").equalsIgnoreCase(format))
                        .findFirst().orElse(null);
                if (arbitrationPre != null){
                    GlobalStates.Arbitration arbitration = new GlobalStates.Arbitration();
                    arbitration.setActivation(arbitrationPre.getActivation());
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(arbitrationPre.getActivation().toInstant(), ZoneOffset.ofHours(8));
                    Instant instant = localDateTime.plusHours(1).toInstant(ZoneOffset.ofHours(8));
                    arbitration.setExpiry(Date.from(instant));
                    arbitration.setNode(arbitrationPre.getNode()+" ("+arbitrationPre.getPlanet()+")");
                    arbitration.setType(arbitrationPre.getType());
                    arbitration.setEnemy(arbitrationPre.getEnemy());
                    arbitration.setArchwing(false);
                    arbitration.setSharkwing(false);
                    arbitration.setEtc(DateUtils.getDiff(Date.from(instant), new Date(), true));
                    state.setArbitration(arbitration);
                }
            }
        }
        Objects.requireNonNull(cm.getCache(WARFRAME_SOCKET_DATA)).put("data", state);
        FileUtils.writeFile("./data/status", JSON.toJSONBytes(state));
    }

    /**
     * 设置缓存
     *
     * @param name 缓存名称
     * @param map  map k,v
     */
    public static void set(String name, Map<Object, Object> map) {
        map.forEach((k, v) -> {
            Objects.requireNonNull(cm.getCache(name)).put(k, v);
        });
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
        return Objects.requireNonNull(Objects.requireNonNull(cm.getCache(name)).get(key)).get();
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @param key  key
     * @param type 缓存的类
     * @return type参数的类
     */
    public static <T> T get(String name, Object key, @Nullable Class<T> type) {
        return Objects.requireNonNull(cm.getCache(name)).get(key, type);
    }
}
