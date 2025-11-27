package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class MarketLichsSisterUtils {

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    public static MarketLichSisterResult getAuctions(String key, SearchType searchType) {
        log.info("Select Market Lich/Sister Strat.");
        MarketLichSisterResult result = queryLichSisterWeapons(key);
        if (!result.getPossibleItems().isEmpty()) {
            return result;
        }


        return result;
    }

    /**
     * 执行市场搜索请求
     * 根据提供的MarketSearchResult对象构建URL参数并发送GET请求到Warframe市场API
     *
     * @param msr MarketSearchResult对象，包含搜索参数
     * @return HttpUtils.Body 响应体
     */
    private static HttpUtils.Body marketSearch(MarketSearchResult msr) {
        return HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SEARCH, msr.getUrl());
    }


    /**
     * 根据关键字获取Lich/Sister武器信息 </br>
     * 该方法会尝试多种匹配方式来查找武器信息: </br>
     * 1. 精确匹配原始关键字</br>
     * 2. 模糊匹配原始关键字</br>
     * 3. 精确匹配别名转换后的关键字</br>
     * 4. 模糊匹配别名转换后的关键字</br>
     * 5. 正则表达式匹配匹配原始关键字</br>
     * 6. 尝试正则表达式转换后的关键字</br>
     * 如果以上都失败，则设置可能的候选项目列表
     *
     * @param key 查询关键字
     * @return MarketLichSister对象，包含匹配的武器信息或候选列表
     */
    private static MarketLichSisterResult queryLichSisterWeapons(String key) {
        log.info("Market Lich/Sister Select ServerDB Key:{}", key);
        var lswRepository = SpringUtils.getBean(LichSisterWeaponsRepository.class);
        var aliasRepository = SpringUtils.getBean(AliasRepository.class);
        MarketLichSisterResult lsw = new MarketLichSisterResult();
        if (tryMatch(lsw, key, lswRepository::findByName)) {
            log.info("Market Lich/Sister Select ServerDB FindByName OK");
            return lsw;
        }
        if (tryMatch(lsw, key, lswRepository::findFirstByNameContaining)) {
            log.info("Market Lich/Sister Select ServerDB FindFirstByNameContaining OK");
            return lsw;
        }
        String alias = processAliases(key, aliasRepository);
        if (tryMatch(lsw, alias, lswRepository::findByName)) {
            log.info("Market Lich/Sister Select ServerDB FindByNameForAlias OK");
            return lsw;
        }
        if (tryMatch(lsw, alias, lswRepository::findFirstByNameContaining)) {
            log.info("Market Lich/Sister Select ServerDB FindFirstByNameContainingForAlias OK");
            return lsw;
        }

        if (tryRegexMatch(lsw, key, lswRepository)) {
            log.info("Market Lich/Sister Select ServerDB Regex OK");
            return lsw;
        }
        if (tryRegexMatch(lsw, alias, lswRepository)) {
            log.info("Market Lich/Sister Select ServerDB RegexForAlias OK");
            return lsw;
        }
        lsw.setPossibleItems(getPossibleItems(key, lswRepository));
        log.warn("Market Lich/Sister Select ServerDB Failed");
        return lsw;
    }

    /**
     * 处理别名替换
     */
    private static String processAliases(String result, AliasRepository aliasRepository) {
        List<Alias> aliases = aliasRepository.findAll();

        for (Alias alias : aliases) {
            if (result.contains(alias.getCn())) {
                return result.replace(alias.getCn(), alias.getEn());
            }
        }
        return result;
    }

    /**
     * 尝试匹配武器信息</br>
     * 通过指定的查找函数来查找武器信息，如果找到则将其设置到MarketLichSister对象中
     *
     * @param ml     用于存储匹配结果的MarketLichSister对象
     * @param key    查找关键字
     * @param lookup 查找函数，接收关键字返回Optional<LichSisterWeapons>
     * @return 如果找到匹配项返回true，否则返回false
     */
    private static boolean tryMatch(
            MarketLichSisterResult ml,
            String key,
            Function<String, Optional<LichSisterWeapons>> lookup
    ) {
        Optional<LichSisterWeapons> apply = lookup.apply(key);
        apply.ifPresent(ml::setLsw);
        return apply.isPresent();
    }

    /**
     * 获取可能的武器项目列表 </br>
     * 通过关键字的第一个字符进行模糊查询，获取所有匹配的武器名称列表
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

    /**
     * 尝试使用正则表达式匹配武器信息 </br>
     * 通过提取关键字的前缀和后缀构造正则表达式进行匹配 </br>
     * 匹配模式为: ^{前缀}.*?{后缀}.*?
     *
     * @param ml         用于存储匹配结果的MarketLichSister对象
     * @param key        查询关键字
     * @param repository LichSisterWeapons仓库实例
     * @return 如果找到匹配项返回true，否则返回false
     */
    private static boolean tryRegexMatch(MarketLichSisterResult ml, String key, LichSisterWeaponsRepository repository) {
        if (key.length() < 2) {
            return false;
        }
        // 获取最后一个字符
        String end = key.substring(key.length() - 1);

        // 确定最大前缀长度 - 最多4个字符，但不能超过字符串总长度-1
        int maxPrefixLength = Math.min(4, key.length() - 1);
        // 从最长前缀开始尝试，逐步减少到1个字符
        for (int prefixLength = maxPrefixLength; prefixLength >= 1; prefixLength--) {
            String header = key.substring(0, prefixLength);
            log.info("Select Regex Parameter - {} : Prefix: '{}' -- Suffix: '{}'", prefixLength, header, end);
            Optional<LichSisterWeapons> items = repository.findByNameRegex("^" + header + ".*?" + end + ".*?");
            if (items.isPresent()) {
                ml.setLsw(items.get());
                return true;
            }
        }

        return false;
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
            return "?type=" +
                    getType().getType() +
                    "&has_ephemera=" +
                    hasEphemera +
                    "&sort_by=" +
                    getSortBy().getValue() +
                    "&weapon_url_name=" +
                    getUrlName() +
                    "&element=" +
                    getElement().getElement() +
                    "&damage_min=" +
                    getDamageMin() +
                    "&damage_max=" +
                    getDamageMax();
        }
    }

    @Data
    @Accessors(chain = true)
    public static class MarketLichSisterResult {
        LichSisterWeapons lsw;
        MarketLichSister mls;
        List<String> possibleItems;
    }

}
