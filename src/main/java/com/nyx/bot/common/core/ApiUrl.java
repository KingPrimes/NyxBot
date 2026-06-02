package com.nyx.bot.common.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.SystemInfoUtils;
import com.nyx.bot.utils.gitutils.CdnTagResolver;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.Arbitration;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    /**
     * Market 拍卖
     */
    public static final String WARFRAME_MARKET_SEARCH = "https://api.warframe.market/v1/auctions/search";
    public static final String WARFRAME_ARBITRATION = "https://wf.555590.xyz/api/arbys?days=30";

    /**
     * Warframe 别名数据源
     */
    public static List<String> warframeDataSourceAlias() {
        return CdnTagResolver.buildUrls("warframe/alias.json");
    }

    /**
     * RivenTion 紫卡属性查询参数
     */
    public static List<String> warframeDataSourceMarketRivenTion() {
        return CdnTagResolver.buildUrls("warframe/market_riven_tion.json");
    }

    /**
     * RivenTion 紫卡属性查询别名
     */
    public static List<String> warframeDataSourceMarketRivenTionAlias() {
        return CdnTagResolver.buildUrls("warframe/market_riven_tion_alias.json");
    }

    /**
     * 星图节点数据源
     */
    public static List<String> warframeDataSourceNodes() {
        return CdnTagResolver.buildUrls("warframe/nodes.json");
    }

    /**
     * 奖励池数据源
     */
    public static List<String> warframeDataSourceRewardPool() {
        return CdnTagResolver.buildUrls("warframe/reward_pool.json");
    }

    /**
     * 紫卡计算器数据源
     */
    public static List<String> warframeDataSourceRivenAnalyseTrend() {
        return CdnTagResolver.buildUrls("warframe/riven_analyse_trend.json");
    }

    /**
     * 状态翻译数据源
     */
    public static List<String> warframeDataSourceStateTranslation() {
        return CdnTagResolver.buildUrls("warframe/state_translation.json");
    }

    private static ObjectMapper getObjectMapper() {
        return ObjectMapperHolder.INSTANCE;
    }

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
            // 获取当前Maven项目版本号
            String version = SystemInfoUtils.getJarVersion();
            // 创建专属请求头
            Map<String, List<String>> headers = Map.of(
                    "User-Agent", List.of("NyxBot/" + version)
            );
            HttpUtils.Body body = HttpUtils.sendGet(WARFRAME_ARBITRATION, "", headers);
            if (!body.is2xxSuccessful()) {
                log.warn("{}", I18nUtils.message("error.warframe.arbitration"));
                return Collections.emptyList();
            }
            return getObjectMapper().readValue(body.body(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("解析仲裁数据失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static class ObjectMapperHolder {
        static final ObjectMapper INSTANCE = SpringUtils.getBean(ObjectMapper.class);
    }
}
