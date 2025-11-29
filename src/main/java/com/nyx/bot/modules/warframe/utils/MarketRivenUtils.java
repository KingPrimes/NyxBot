package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.entity.RivenTion;
import com.nyx.bot.modules.warframe.entity.RivenTionAlias;
import com.nyx.bot.modules.warframe.enums.MarketSearchPolicy;
import com.nyx.bot.modules.warframe.enums.MarketSortBy;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.modules.warframe.repo.RivenTionAliasRepository;
import com.nyx.bot.modules.warframe.repo.RivenTionRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.market.MarketRiven;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class MarketRivenUtils {

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);


    /**
     * 获取紫卡武器信息 如果遇到为查询到的物品则返回可能要查询的物品列表
     * RivenItems.items 可能要查询的物品列表
     */
    private static RivenItems getRiveItems(String key) {
        log.debug("------紫卡查询--查询数据库------");
        log.debug("原始数据：{}", key);
        // 假设用户输入的是正确值，直接查询数据库
        var repository = SpringUtils.getBean(RivenItemsRepository.class);
        Optional<RivenItems> items = repository.findByName(key);
        log.debug("假设正确:{}", items.orElse(new RivenItems()));
        if (items.isPresent()) {
            return items.get();
        }

        // 假设用户输入的是别名，查询数据库
        var alias = SpringUtils.getBean(AliasRepository.class);
        key = key.toLowerCase(Locale.ROOT);
        if (!key.contains("prime") && key.contains("p")) {
            key = key.replace("p", "Prime");
        }
        Optional<Alias> a = alias.findByCn(key);
        if (a.isPresent()) {
            items = repository.findByName(a.get().getEn());
            log.debug("别名:{}", items);
            if (items.isPresent()) {
                return items.get();
            }
        }

        // 正则查询
        String start = String.valueOf(key.charAt(0));
        String end = String.valueOf(key.charAt(key.length() - 1));
        items = repository.findByNameRegex("^" + start + ".*?" + end + ".*?");
        log.debug("正则查询:{}", items);
        if (items.isPresent()) {
            return items.get();
        }

        //最后查询所有以该字符开头的物品，并返回
        List<RivenItems> itemsList = repository.nameLikes(start);
        RivenItems finalItems = new RivenItems();
        finalItems.setItems(itemsList);
        log.debug("可能存在的值：{}", finalItems);
        log.debug("------数据库查询结束------");
        return finalItems;
    }

    /**
     * 从指定URL获取紫卡市场数据并进行处理
     * <p>发送HTTP请求获取市场数据，解析JSON响应，并对数据进行过滤和排序处理</p>
     *
     * @param params   请求的URL地址
     * @param itemName 物品名称
     * @return MarketRiven对象，包含处理后的市场数据，失败时返回空对象
     */
    private static MarketRiven fetchAndProcess(MarketSearchResult params, String itemName) {
        MarketRiven empty = new MarketRiven();
        // 发送HTTP GET请求获取市场数据
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SEARCH, params.getUrl());

        // 检查HTTP响应状态码
        if (!body.code().is2xxSuccessful()) {
            log.debug("获取数据失败：{}", body.body());
            return empty;
        }

        try {
            // 解析JSON数据并进行流式处理
            empty = stream(objectMapper.readValue(body.body(), MarketRiven.class), itemName);
            log.debug("获取到的数据：\n{}", empty);
            return empty;
        } catch (Exception e) {
            log.error("解析紫卡数据失败: {}", e.getMessage());
            return empty;
        }
    }

    /**
     * 解析负面词条参数
     * <p>从参数列表中提取第三个元素作为负面词条，并将其转换为英文标识符</p>
     *
     * @param list 参数列表，通常包含武器名称、正面词条和负面词条
     * @return 转换后的负面词条字符串，"has"表示有负面词条，"none"表示无负面词条，列表长度不足时返回空字符串
     */
    private static String parseNegativeStat(List<String> list) {
        // 检查列表长度是否足够包含负面词条参数
        if (list.size() <= 2) {
            return "";
        }
        // 提取负面词条并转换中文标识为英文
        return list.get(2)
                .replace("有", "has")
                .replace("无", "none");
    }

    /**
     * 解析词条效果的URL名称
     * <p>将词条效果名称转换为URL友好的标识符，支持直接匹配和别名匹配</p>
     *
     * @param stats 词条效果数组
     * @return 逗号分隔的URL名称字符串
     */
    private static String resolveStatUrlNames(String[] stats) {
        RivenTionRepository tionRepo = SpringUtils.getBean(RivenTionRepository.class);
        RivenTionAliasRepository aliasRepo = SpringUtils.getBean(RivenTionAliasRepository.class);

        return Arrays.stream(stats)
                .map(String::trim)
                // 将每个词条效果映射为URL名称
                .map(effect -> tionRepo.findByEffect(effect)
                        .map(RivenTion::getUrlName)
                        // 如果找不到直接匹配，则尝试通过别名查找
                        .orElseGet(() -> aliasRepo.findByCn(effect)
                                .map(RivenTionAlias::getEn)
                                .orElse(null)))
                // 过滤掉无法识别的词条
                .filter(Objects::nonNull)
                // 将所有URL名称用逗号连接
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    /**
     * 解析返回数据
     *
     * @param marketRiven 数据
     * @param itemName    物品名称
     */
    private static MarketRiven stream(MarketRiven marketRiven, String itemName) {
        marketRiven.setItemName(itemName);
        marketRiven.getPayload().setAuctions(
                marketRiven.getPayload()
                        .getAuctions().stream()
                        // 过滤掉已结束的物品
                        .filter(m -> !m.getClosed())
                        // 过滤掉已下架的物品
                        .filter(MarketRiven.Auctions::getVisible)
                        // 过滤掉不在线玩家
                        .filter(m -> m.getOwner().getStatus().equals("online") || m.getOwner().getStatus().equals("ingame"))
                        // 排序
                        .sorted(MarketRivenUtils::compareByPrice)
                        // 取前10个
                        .limit(10)
                        .peek(MarketRivenUtils::mapAttributeEffects)
                        .toList());
        // 解析返回数据
        return marketRiven;
    }

    /**
     * 根据价格信息比较两个拍卖物品的优先级
     * <p>优先比较买断价格，其次是比较起始价格，最后比较最高出价</p>
     *
     * @param o1 第一个拍卖物品
     * @param o2 第二个拍卖物品
     * @return 比较结果：负数表示o1价格更低，0表示价格相等，正数表示o1价格更高
     */
    private static int compareByPrice(MarketRiven.Auctions o1, MarketRiven.Auctions o2) {
        // 优先比较买断价格
        if (o1.getBuyoutPrice() != null && o2.getBuyoutPrice() != null) {
            return o1.getBuyoutPrice() - o2.getBuyoutPrice();
        }
        // 其次比较起始价格
        if (o1.getStartingPrice() != null && o2.getStartingPrice() != null) {
            return o1.getStartingPrice() - o2.getStartingPrice();
        }
        // 最后比较最高出价
        if (o1.getTopBid() != null && o2.getTopBid() != null) {
            return o1.getTopBid() - o2.getTopBid();
        }
        return 0;
    }

    /**
     * 映射拍卖物品属性效果名称
     * <p>将拍卖物品的属性URL名称转换为实际的效果描述文本</p>
     *
     * @param auction 拍卖物品对象，包含待转换的属性信息
     */
    private static void mapAttributeEffects(MarketRiven.Auctions auction) {
        RivenTionRepository repo = SpringUtils.getBean(RivenTionRepository.class);
        auction.getItem().setAttributes(
                auction.getItem().getAttributes().stream()
                        .peek(a -> a.setUrlName(
                                repo.findByUrlName(a.getUrlName())
                                        .orElse(new RivenTion())
                                        .getEffect()
                        ))
                        .toList()
        );
    }

    /**
     * 根据关键字查询紫卡市场信息
     * <p>支持两种查询模式：无词条参数的简单查询和带词条参数的高级查询</p>
     *
     * @param key 查询关键字，格式可以是"武器名称"或"武器名称-正面词条1,正面词条2-负面词条"
     * @return MarketRiven对象，包含查询结果或可能的物品列表
     */
    public static MarketRiven marketRivenParameter(String key) {
        log.debug("------紫卡查询--构建请求地址------");
        MarketRiven marketRiven = new MarketRiven();
        RivenItems riveItems;
        // 无词条参数查询模式
        MarketSearchResult result = new MarketSearchResult();
        if (!key.contains("-")) {
            log.debug("------无词条参数状态------");
            riveItems = getRiveItems(key.trim());
            // 判断是否有查询到物品,如果没有则返回可能要查询的物品列表
            if (riveItems.getItems() != null) {
                //marketRiven.setPossibleItems(riveItems.getItems());
                log.debug("------未查询到匹配的物品------");
                return marketRiven;
            }
            // 构建无参数请求url
            result
                    .setUrlName(riveItems.getSlug())
                    .setPositiveStats("")
                    .setNegativeStats("");
            return fetchAndProcess(result, riveItems.getName());
        }
        // 有词条参数查询模式
        log.debug("------有词条参数状态------");
        // 有请求参数 以-分割
        List<String> list = Arrays.stream(key.split("-")).toList();
        riveItems = getRiveItems(list.getFirst().trim());
        // 判断是否有查询到物品,如果没有则返回可能要查询的物品列表
        if (riveItems.getItems() != null) {
            log.debug("------未查询到匹配的物品------");
            //marketRiven.setPossibleItems(riveItems.getItems());
            return marketRiven;
        }

        // 解析正面词条参数
        String positiveStats = resolveStatUrlNames(
                list.get(1).replaceAll("，", ",").split(",")
        );
        // 解析负面词条参数
        String not = parseNegativeStat(list);

        log.debug("正面词条：{} ---- 负面词条:{}", positiveStats, not);
        // 构建带参数请求url
        result
                .setUrlName(riveItems.getSlug())
                .setPositiveStats(positiveStats)
                .setNegativeStats(not)
                .setPolarity(Polarity.ANY)
                .setMasteryRankMin(7)
                .setMasteryRankMax(16)
                .setBuyoutPolicy(MarketSearchPolicy.ANY)
                .setSortBy(MarketSortBy.PRICE_ASC);
        return fetchAndProcess(result, riveItems.getName());
    }

    /**
     * 极性
     */
    private enum Polarity {
        ANY,
        MADURAI,
        VAZARIN,
        NARAMON
    }

    @Data
    @Accessors(chain = true)
    private static class MarketSearchResult {
        /**
         * url名称
         */
        String urlName;
        //正向词条以,分割
        String positiveStats;
        //负面词条 none 无  has 有
        String negativeStats;
        //极性
        Polarity polarity = Polarity.ANY;
        //最低段位
        Integer masteryRankMin = 7;
        //最高段位
        Integer masteryRankMax = 16;
        //类别 direct 售卖  auction 拍卖 默认全部
        MarketSearchPolicy buyoutPolicy = MarketSearchPolicy.ANY;
        //排序方式  price_asc 价格正序, price_desc 价格倒序, damage_asc 伤害正序, damage_desc 伤害倒序
        MarketSortBy sortBy = MarketSortBy.PRICE_ASC;

        public Integer getMasteryRankMin() {
            if (masteryRankMin == null || masteryRankMin < 7) return 7;
            return masteryRankMin;
        }

        public Integer getMasteryRankMax() {
            if (masteryRankMax == null || masteryRankMax > 16) return 16;
            return masteryRankMax;
        }

        public String getUrl() {
            StringBuilder url = new StringBuilder();
            url.append("type=riven&weapon_url_name=")
                    .append(getUrlName());
            if (!getPositiveStats().trim().isEmpty()) {
                url.append("&positive_stats=").append(getPositiveStats());
            }
            if (!getBuyoutPolicy().equals(MarketSearchPolicy.ANY)) {
                url.append("&buyout_policy=").append(getBuyoutPolicy().name());
            }
            if (!getNegativeStats().trim().isEmpty()) {
                url.append("&negative_stats=").append(getNegativeStats());
            }
            url.append("&mastery_rank_min=")
                    .append(getMasteryRankMin())
                    .append("&mastery_rank_max=")
                    .append(getMasteryRankMax())
                    .append("&polarity=").append(getPolarity().name())
                    .append("&sort_by=").append(getSortBy().getValue());
            return url.toString().toLowerCase(Locale.ROOT);
        }
    }

}
