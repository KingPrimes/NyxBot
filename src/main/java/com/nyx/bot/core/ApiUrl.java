package com.nyx.bot.core;

import com.nyx.bot.utils.http.HttpUtils;
import okhttp3.Headers;

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
    public static final String WARFRAME_DATA_SOURCE_GIT = "https://github.com/KingPrimes/DataSource";

    /**
     * 战甲数据源
     */
    public static final String WARFRAME_DATA_SOURCE_GIT_HUB = "https://raw.github.com/KingPrimes/DataSource/main/warframe/";
    /**
     * 战甲数据源 备用源
     */
    public static final String WARFRAME_DATA_SOURCE_GIT_CODE = "https://raw.gitcode.com/KingPrimes/DataSource/raw/main/warframe/";


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
    public static final String WARFRAME_MARKET_Riven_ITEMS = "https://api.warframe.market/v1/riven/items";

    /**
     * 赤毒武器
     */
    public static final String WARFRAME_MARKET_LICH_WEAPONS = "https://api.warframe.market/v1/lich/weapons";

    /**
     * 信条武器
     */
    public static final String WARFRAME_MARKET_SISTER_WEAPONS = "https://api.warframe.market/v1/sister/weapons";


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

}
