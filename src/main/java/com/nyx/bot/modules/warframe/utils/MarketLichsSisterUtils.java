package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.enums.MarketSortBy;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.repo.LichSisterWeaponsRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.market.MarketLichSister;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.List;

@Slf4j
public class MarketLichsSisterUtils {

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    /**
     * 获取Lich/Sister武器拍卖信息
     *
     * @param key        武器名称关键字
     * @param searchType 搜索类型（LICH或SISTER）
     * @return MarketLichSisterResult包含武器信息和拍卖数据
     */
    public static MarketResult<LichSisterWeapons, MarketLichSister> getAuctions(String key, SearchType searchType) {
        log.debug("开始查询 Market Lich/Sister 拍卖，关键字: {}, 类型: {}", key, searchType.getType());

        // 第一步：查询武器信息
        MarketResult<LichSisterWeapons, MarketLichSister> result = queryLichSisterWeapons(key);

        // 如果未找到武器，返回可能的物品列表
        if (result.getEntity() == null) {
            log.warn("未找到匹配的武器: {}", key);
            return result;
        }

        log.debug("找到武器: {}, 开始查询拍卖数据", result.getEntity().getName());

        // 第二步：构建搜索参数
        MarketSearchResult searchParams = new MarketSearchResult()
                .setType(searchType)
                .setUrlName(result.getEntity().getSlug())
                .setHasEphemera(false)
                .setSortBy(MarketSortBy.PRICE_ASC)
                .setElement(MarketSearchElementEnum.ANY)
                .setDamageMin(25)
                .setDamageMax(60);

        // 第三步：执行API查询
        try {
            MarketLichSister auctionData = fetchAuctions(searchParams);
            result.setResult(auctionData);
            log.debug("成功获取拍卖数据，武器: {}, 拍卖数量: {}",
                    result.getEntity().getName(),
                    auctionData.getPayload() != null ? auctionData.getPayload().getAuctions().size() : 0);
        } catch (Exception e) {
            log.error("查询拍卖数据失败，武器: {}, 错误: {}", result.getEntity().getName(), e.getMessage(), e);
            throw new RuntimeException("查询Market拍卖数据失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 从Warframe Market获取Lich/Sister拍卖数据
     * <p>发送HTTP请求到Market API，获取拍卖信息并进行数据处理</p>
     *
     * @param searchParams 搜索参数对象
     * @return MarketLichSister对象，包含拍卖数据
     * @throws RuntimeException 当API调用失败或解析失败时
     */
    private static MarketLichSister fetchAuctions(MarketSearchResult searchParams) {
        String url = ApiUrl.WARFRAME_MARKET_SEARCH;
        String params = searchParams.getUrl();

        log.debug("查询拍卖 URL: {}{}", url, params);

        // 发送HTTP请求
        HttpUtils.Body body = HttpUtils.marketSendGet(url, params);

        // 检查HTTP状态码
        if (!body.code().is2xxSuccessful()) {
            if (body.code().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new RuntimeException("触发速率限制，请稍后再次尝试查询。");
            }
            throw new RuntimeException(
                    "查询Market数据失败, Code: %d Headers: %s".formatted(
                            body.code().value(),
                            body.headers().toSingleValueMap().toString()
                    )
            );
        }

        try {
            // 解析JSON响应
            MarketLichSister marketData = objectMapper.readValue(
                    body.body(),
                    MarketLichSister.class
            );

            // 数据过滤和处理
            return processAuctionData(marketData);

        } catch (Exception e) {
            log.error("解析Market拍卖数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理拍卖数据：过滤离线用户、已关闭拍卖，按价格排序
     *
     * @param marketData 原始市场数据
     * @return 处理后的数据
     */
    private static MarketLichSister processAuctionData(MarketLichSister marketData) {
        if (marketData.getPayload() == null ||
                marketData.getPayload().getAuctions() == null) {
            log.warn("拍卖数据为空");
            return marketData;
        }

        int originalSize = marketData.getPayload().getAuctions().size();

        var filteredAuctions = marketData.getPayload()
                .getAuctions()
                .stream()
                // 过滤已关闭的拍卖
                .filter(auction -> !auction.getClosed())
                // 过滤不可见的拍卖
                .filter(MarketLichSister.Auctions::getVisible)
                // 过滤离线用户
                .filter(auction -> {
                    String status = auction.getOwner().getStatus();
                    return "ingame".equals(status) || "online".equals(status);
                })
                // 按价格排序（优先买断价，其次起始价，最后最高出价）
                .sorted((a1, a2) -> {
                    // 优先使用买断价
                    if (a1.getBuyoutPrice() != null && a2.getBuyoutPrice() != null) {
                        return Integer.compare(a1.getBuyoutPrice(), a2.getBuyoutPrice());
                    }
                    // 其次使用起始价
                    if (a1.getStartingPrice() != null && a2.getStartingPrice() != null) {
                        return Integer.compare(a1.getStartingPrice(), a2.getStartingPrice());
                    }
                    // 最后使用最高出价
                    if (a1.getTopBid() != null && a2.getTopBid() != null) {
                        return Integer.compare(a1.getTopBid(), a2.getTopBid());
                    }
                    return 0;
                })
                // 限制返回数量
                .limit(10)
                .toList();

        marketData.getPayload().setAuctions(filteredAuctions);

        log.debug("拍卖数据处理完成：原始数量: {}, 过滤后数量: {}", originalSize, filteredAuctions.size());
        return marketData;
    }


    /**
     * 根据关键字获取Lich/Sister武器信息
     * <p>该方法会尝试多种匹配方式来查找武器信息:</p>
     * <ol>
     * <li>精确匹配原始关键字</li>
     * <li>模糊匹配原始关键字</li>
     * <li>精确匹配别名转换后的关键字</li>
     * <li>模糊匹配别名转换后的关键字</li>
     * <li>正则表达式匹配原始关键字</li>
     * <li>正则表达式匹配别名转换后的关键字</li>
     * </ol>
     * <p>如果以上都失败，则设置可能的候选项目列表</p>
     *
     * @param key 查询关键字
     * @return MarketLichSisterResult对象，包含匹配的武器信息或候选列表
     */
    private static MarketResult<LichSisterWeapons, MarketLichSister> queryLichSisterWeapons(String key) {
        log.info("Market Lich/Sister Query DataBase Key: {}", key);

        try {
            var lswRepository = SpringUtils.getBean(LichSisterWeaponsRepository.class);
            var aliasRepository = SpringUtils.getBean(AliasRepository.class);
            MarketResult<LichSisterWeapons, MarketLichSister> mr = new MarketResult<>();
            // 1. 精确匹配原始关键字
            if (MarketCommonUtils.tryMatch(mr, key, lswRepository::findByName)) {
                log.info("Market Lich/Sister FindByName OK");
                return mr;
            }

            // 2. 模糊匹配原始关键字
            if (MarketCommonUtils.tryMatch(mr, key, lswRepository::findFirstByNameContaining)) {
                log.info("Market Lich/Sister FindFirstByNameContaining OK");
                return mr;
            }

            // 3. 别名转换
            String alias = MarketCommonUtils.processAliases(key, aliasRepository);
            if (!alias.equals(key)) {
                log.info("Alias Conversion: {} -> {}", key, alias);
            }

            // 4. 精确匹配别名
            if (MarketCommonUtils.tryMatch(mr, alias, lswRepository::findByName)) {
                log.info("Market Lich/Sister FindByNameForAlias OK");
                return mr;
            }

            // 5. 模糊匹配别名
            if (MarketCommonUtils.tryMatch(mr, alias, lswRepository::findFirstByNameContaining)) {
                log.info("Market Lich/Sister FindFirstByNameContainingForAlias OK");
                return mr;
            }

            // 6. 正则匹配原始关键字
            if (MarketCommonUtils.tryRegexNameMatch(mr, key, lswRepository)) {
                log.info("Market Lich/Sister Regex OK");
                return mr;
            }

            // 7. 正则匹配别名
            if (MarketCommonUtils.tryRegexNameMatch(mr, alias, lswRepository)) {
                log.info("Market Lich/Sister RegexForAlias OK");
                return mr;
            }

            // 8. 返回可能的候选项
            mr.setPossibleItems(getPossibleItems(key, lswRepository));
            log.warn("Market Lich/Sister No matching weapon found, return the number of candidates: {}", mr.getPossibleItems().size());
            return mr;

        } catch (Exception e) {
            throw new RuntimeException("Market Lich/Sister Query DataBase Error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取可能的武器项目列表
     * <p>通过关键字的第一个字符进行模糊查询，获取所有匹配的武器名称列表</p>
     *
     * @param key        查询关键字
     * @param repository LichSisterWeapons仓库实例
     * @return 包含武器名称的字符串列表
     */
    private static List<String> getPossibleItems(String key, LichSisterWeaponsRepository repository) {
        List<LichSisterWeapons> list = repository.findByNameContaining(String.valueOf(key.charAt(0)));
        return list.stream()
                .map(LichSisterWeapons::getName)
                .toList();
    }

    @Getter
    public enum SearchType {
        /**
         * 赤毒玄骸
         */
        LICH("lich"),
        /**
         * 帕尔沃斯的姐妹
         */
        SISTER("sister"),
        ;
        private final String type;

        SearchType(String type) {
            this.type = type;
        }
    }

    @Getter
    public enum MarketSearchElementEnum {
        /**
         * 冰冻
         */
        COLD("cold"),
        /**
         * 辐射
         */
        RADIATION("radiation"),
        /**
         * 火焰
         */
        HEAT("heat"),
        /**
         * 磁力
         */
        MAGNETIC("magnetic"),
        /**
         * 毒素
         */
        TOXIN("toxin"),
        /**
         * 电击
         */
        ELECTRICITY("electricity"),
        /**
         * 冲击
         */
        IMPACT("impact"),
        /**
         * 任意元素
         */
        ANY("any"),
        ;
        private final String element;

        MarketSearchElementEnum(String element) {
            this.element = element;
        }
    }

    @Data
    @Accessors(chain = true)
    private static class MarketSearchResult {
        /**
         * 类型
         */
        SearchType type;
        /**
         * 是否携带幻纹 false不携带 true携带
         */
        Boolean hasEphemera = false;
        /**
         * 排序方式
         */
        MarketSortBy sortBy = MarketSortBy.PRICE_ASC;
        /**
         * url名称
         */
        String urlName;
        /**
         * 元素
         */
        MarketSearchElementEnum element = MarketSearchElementEnum.ANY;
        /**
         * 最小伤害
         */
        Integer damageMin = 25;
        /**
         * 最大伤害
         */
        Integer damageMax = 60;

        public Integer getDamageMin() {
            if (damageMin < 25) return 25;
            return damageMin;
        }

        public Integer getDamageMax() {
            if (damageMax > 60) return 60;
            return damageMax;
        }

        public String getUrl() {
            StringBuilder url = new StringBuilder();
            url.append("type=").append(getType().getType());
            url.append("&has_ephemera=").append(hasEphemera);
            url.append("&sort_by=").append(getSortBy().getValue())
                    .append("&weapon_url_name=").append(getUrlName());
            if (!getElement().equals(MarketSearchElementEnum.ANY)) {
                url.append("&element=").append(getElement().getElement());
            }
            url.append("&damage_min=").append(getDamageMin())
                    .append("&damage_max=").append(getDamageMax());
            return url.toString();
        }
    }

}
