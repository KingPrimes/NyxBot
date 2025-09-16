package com.nyx.bot.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.x;
import okhttp3.Headers;

import java.util.List;

public class ApiUrl {


    /**
     * 中文
     */
    public static final Headers LANGUAGE_ZH_HANS = Headers.of("language", "zh-hans");

    public static final String WARFRAME_SOCKET = "ws://api.warframestat.us/socket";

    public static final String WARFRAME_STATUS = "https://api.warframestat.us/";

    /**
     * 战甲数据源 git push pull等操作链接
     */
    public static final List<String> DATA_SOURCE_GIT = List.of(
            "https://github.com/KingPrimes/DataSource",
            "https://gitcode.com/KingPrimes/DataSource",
            "https://gitlab.com/KingPrimes/DataSource",
            "https://gitee.com/KingPrime/DataSource"
    );

    /**
     * 赤毒幻纹
     */
    public static final String WARFRAME_MARKET_LICH_EPHEMERAS = "https://api.warframe.market/v1/lich/ephemeras";

    /**
     * 信条幻纹
     */
    public static final String WARFRAME_MARKET_SISTER_EPHEMERAS = "https://api.warframe.market/v1/sister/ephemeras";

    /**
     * Market 物品
     */
    public static final String WARFRAME_MARKET_ITEMS = "https://api.warframe.market/v1/items";

    /**
     * Market 紫卡武器
     */
    public static final String WARFRAME_MARKET_RIVEN_ITEMS = "https://api.warframe.market/v1/riven/items";

    /**
     * 赤毒武器
     */
    public static final String WARFRAME_MARKET_LICH_WEAPONS = "https://api.warframe.market/v1/lich/weapons";

    /**
     * 信条武器
     */
    public static final String WARFRAME_MARKET_SISTER_WEAPONS = "https://api.warframe.market/v1/sister/weapons";

    public static final String WARFRAME_RELICS_DATA = "https://drops.warframestat.us/data/relics.json";

    private static final String WARFRAME_PROFILE = "https://api.warframestat.us/profile/%s";

    private static final String WARFRAME_PROFILE_STATS = "https://api.warframestat.us/profile/%s/stats";

    /**
     * Market 物品查询
     *
     * @param key  物品名称
     * @param from 平台
     * @return 返回信息
     */
    public static HttpUtils.Body marketOrders(String key, String from) {
        String url = "https://api.warframe.market/v1/items/" + key + "/orders?include=item";
        return HttpUtils.sendGet(url, Headers.of("platform", from));
    }

    /**
     * 获取仲裁信息
     *
     * @return 仲裁
     */
    public static List<ArbitrationPre> arbitrationPreList(String key) {
        return JSON.parseArray(HttpUtils.sendGet(x.d().formatted(key)).getBody(), ArbitrationPre.class, JSONReader.Feature.SupportSmartMatch);
    }

    /**
     * 获取Warframe的个人信息
     *
     * @param name ID
     * @return 个人信息
     */
    public static HttpUtils.Body getProfile(String name) {
        return HttpUtils.sendGet(String.format(WARFRAME_PROFILE, name));
    }

    /**
     * 获取Warframe的个人统计信息
     *
     * @param name ID
     * @return 统计信息
     */
    public static HttpUtils.Body getProfileStats(String name) {
        return HttpUtils.sendGet(String.format(WARFRAME_PROFILE_STATS, name));
    }

}
