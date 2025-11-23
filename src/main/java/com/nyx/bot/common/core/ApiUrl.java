package com.nyx.bot.common.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.Arbitration;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
@Slf4j
public class ApiUrl {

    // 官方数据源
    public static final String WARFRAME_WORLD_STATE = "https://api.warframe.com/cdn/worldState.php";
    // 官方图片获取地址
    public static final String WARFRAME_PUBLIC_EXPORT = "http://content.warframe.com/PublicExport/%s";
    public static final String WARFRAME_PUBLIC_EXPORT_MANIFESTS = "http://content.warframe.com/PublicExport/Manifest/%s";
    public static final String WARFRAME_PUBLIC_EXPORT_INDEX = "https://origin.warframe.com/PublicExport/index_%s.txt.lzma";
    /**
     * Warframe 别名数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_ALIAS = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/alias.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/alias.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/alias.json"
    );
    /**
     * RivenTion 数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_MARKET_RIVEN_TION = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/market_riven_tion.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/market_riven_tion.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/market_riven_tion.json"
    );
    /**
     * RivenTion 别名数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_MARKET_RIVEN_TION_ALIAS = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/market_riven_tion_alias.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/market_riven_tion_alias.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/market_riven_tion_alias.json"
    );
    /**
     * 节点数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_NODES = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/nodes.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/nodes.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/nodes.json"
    );
    /**
     * 奖励池数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_REWARD_POOL = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/reward_pool.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/reward_pool.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/reward_pool.json"
    );
    /**
     * 紫卡计算器数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_RIVEN_ANALYSE_TREND = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/riven_analyse_trend.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/riven_analyse_trend.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/riven_analyse_trend.json"
    );
    /**
     * 状态翻译数据源</br>
     * 使用<a href="https://www.jsdelivr.com/">www.jsdelivr.com</a>与<a href="https://cdn.jsdmirror.com/">cdn.jsdmirror.com</a>进行cnd加速获取数据</br>
     */
    public static final List<String> WARFRAME_DATA_SOURCE_STATE_TRANSLATION = List.of(
            "https://cdn.jsdmirror.com/gh/KingPrimes/DataSource/warframe/state_translation.json",
            "https://cdn.jsdmirror.cn/gh/KingPrimes/DataSource/warframe/state_translation.json",
            "https://cdn.jsdelivr.net/gh/KingPrimes/DataSource/warframe/state_translation.json"
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
    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    /**
     * Market 物品查询
     *
     * @param key  物品名称
     * @param from 平台
     * @return 返回信息
     */
    public static HttpUtils.Body marketOrders(String key, MarketPlatformEnum from) {
        String url = "https://api.warframe.market/v2/orders/item/%s".formatted(key);
        log.debug("MarketOrderURL:{}", url);
        return HttpUtils.marketSendGet(url, "", from);
    }

    /**
     * Market 查询物品详细信息
     *
     * @param key 物品名称
     * @return 物品详细信息
     */
    public static HttpUtils.Body marketOrdersSet(String key, MarketPlatformEnum from) {
        String url = "https://api.warframe.market/v2/item/%s/set".formatted(key);
        log.debug("MarketOrderSetURL:{}", url);
        return HttpUtils.marketSendGet(url, "", from);
    }

    /**
     * 获取仲裁信息
     *
     * @return 仲裁
     */
    public static List<Arbitration> arbitrationPreList() {
        try {
            HttpUtils.Body body = HttpUtils.sendGet(WARFRAME_ARBITRATION);
            if (!body.code().is2xxSuccessful()) {
                log.warn("{}", I18nUtils.message("error.warframe.arbitration"));
                return Collections.emptyList();
            }
            return objectMapper.readValue(body.body(), new TypeReference<List<Arbitration>>() {
            });
        } catch (Exception e) {
            log.error("解析仲裁数据失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
