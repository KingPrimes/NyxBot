package com.nyx.bot.utils;

import com.nyx.bot.res.SocketGlobalStates;
import org.springframework.cache.CacheManager;

public class CacheUtils {
    private static final CacheManager cm = SpringUtils.getBean(CacheManager.class);
    public static final String SYSTEM = "system";
    public static final String WARFRAME_SOCKET_DATA = "warframe-socket-data";
    public static final String GROUP_CAPTCHA = "group-captcha";

    public static SocketGlobalStates getGlobalState() {
        SocketGlobalStates data = cm.getCache(WARFRAME_SOCKET_DATA).get("data", SocketGlobalStates.class);
        if (data == null) {
            throw new RuntimeException("SocketGlobalStates is null");
        }
        return data;
    }

    public static void setGlobalState(SocketGlobalStates state) {
        cm.getCache(WARFRAME_SOCKET_DATA).put("data", state);
    }
}
