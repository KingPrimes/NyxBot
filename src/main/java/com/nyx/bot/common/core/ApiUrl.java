package com.nyx.bot.common.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.res.Arbitration;
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
            "https://gitlab.com/KingPrimes/DataSource.git",
            "https://gitcode.com/KingPrimes/DataSource",
            "https://gitee.com/KingPrime/DataSource"
    );

    /**
     * 赤毒幻纹
     */
    public static final String WARFRAME_MARKET_LICH_EPHEMERAS = "https://api.warframe.market/v2/lich/ephemeras";

    /**
     * 信条幻纹
     */
    public static final String WARFRAME_MARKET_SISTER_EPHEMERAS = "https://api.warframe.market/v2/sister/ephemeras";

    /**
     * Market 物品
     */
    public static final String WARFRAME_MARKET_ITEMS = "https://api.warframe.market/v2/items";

    /**
     * Market 紫卡武器
     */
    public static final String WARFRAME_MARKET_RIVEN_WEAPONS = "https://api.warframe.market/v2/riven/weapons";

    /**
     * 赤毒武器
     */
    public static final String WARFRAME_MARKET_LICH_WEAPONS = "https://api.warframe.market/v2/lich/weapons";

    /**
     * 信条武器
     */
    public static final String WARFRAME_MARKET_SISTER_WEAPONS = "https://api.warframe.market/v2/sister/weapons";

    public static final String WARFRAME_ARBITRATION = "https://wf.555590.xyz/api/arbys?days=30";

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
    public static List<Arbitration> arbitrationPreList() {
        try {
            HttpUtils.Body body = HttpUtils.sendGet(WARFRAME_ARBITRATION);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("{}", I18nUtils.message("error.warframe.arbitration"));
                return Collections.emptyList();
            }
            return JSON.parseArray(body.getBody(), Arbitration.class, JSONReader.Feature.SupportSmartMatch);
        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }
}
