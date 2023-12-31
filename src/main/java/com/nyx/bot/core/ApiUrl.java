package com.nyx.bot.core;

import com.nyx.bot.utils.HttpUtils;
import okhttp3.Headers;

public class ApiUrl {


    /**
     * 中文
     */
    public static final Headers LANGUAGE_ZH_HANS = Headers.of("language", "zh-hans");

    public static final String WARFRAME_SOCKET = "ws://api.warframestat.us/socket";

    /**
     * 战甲数据源
     */
    public static final String WARFRAME_DATA_SOURCE = "https://raw.github.com/KingPrimes/DataSource/main/warframe/";


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
