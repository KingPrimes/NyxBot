package com.nyx.bot.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

import java.util.Collections;
import java.util.List;
@SuppressWarnings("unused")
@Slf4j
public class ApiUrl {


    /**
     * 中文
     */
    public static final Headers LANGUAGE_ZH_HANS = Headers.of("language", "zh-hans");

    public static final String WARFRAME_SOCKET = "ws://api.warframestat.us/socket";

    public static final String WARFRAME_STATUS = "https://api.warframestat.us/";

    // 官方数据源
    public static final String WARFRAME_WORLD_STATE = "https://content.warframe.com/dynamic/worldState.php";

    // 官方图片获取地址
    public static final String WARFRAME_PUBLIC_EXPORT = "http://content.warframe.com/PublicExport/%s";

    public static final String WARFRAME_PUBLIC_EXPORT_MANIFESTS = "http://content.warframe.com/PublicExport/Manifest/%s";

    public static final String WARFRAME_PUBLIC_EXPORT_INDEX = "https://origin.warframe.com/PublicExport/index_%s.txt.lzma";

    /**
     * 战甲数据源 git push pull等操作链接
     */
    public static final List<String> DATA_SOURCE_GIT = List.of(
            "https://github.com/KingPrimes/DataSource",
            "https://jihulab.com/KingPrimes/DataSource",
            "https://gitlab.com/KingPrimes/DataSource.git",
            "https://gitcode.com/KingPrimes/DataSource",
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

    public static final String WARFRAME_ARBITRATION = "https://wf.555590.xyz/api/arbys?days=30&key=%s";

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
        try {
            HttpUtils.Body body = HttpUtils.sendGet(WARFRAME_ARBITRATION.formatted(key));
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("{}", I18nUtils.message("error.warframe.arbitration"));
                return Collections.emptyList();
            }
            if (body.getBody().isEmpty()) {
                log.warn("{}", I18nUtils.message("error.warframe.arbitration"));
                return Collections.emptyList();
            }
            return JSON.parseArray(body.getBody(), ArbitrationPre.class, JSONReader.Feature.SupportSmartMatch);
        } catch (JSONException e) {
            return Collections.emptyList();
        }
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
