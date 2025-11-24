package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import io.github.kingprimes.model.enums.MarketStatusEnum;
import io.github.kingprimes.model.enums.TransactionEnum;
import io.github.kingprimes.model.market.BaseOrder;
import io.github.kingprimes.model.market.OrderWithUser;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
public class MarketOrderUtils {
    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    /**
     * 从数据库中查询可能的值
     *
     * @param key 物品名称
     * @return 处理后的结果
     */
    private static Market toDataBase(String key) {
        Market market = new Market();
        try {
            // 标准化输入
            String normalizedKey = normalizeInput(key);

            // 获取仓库实例
            OrdersItemsRepository itemsRepository = getItemsRepository();
            AliasRepository aliasRepository = getAliasRepository();

            // 尝试不同的查询策略
            if (tryDirectMatch(market, itemsRepository, normalizedKey)) {
                return market;
            }

            String aliasProcessedKey = processAliases(normalizedKey, aliasRepository);
            if (tryAliasMatch(market, itemsRepository, aliasProcessedKey)) {
                return market;
            }

            String primeProcessedKey = processPrimeKeyword(aliasProcessedKey);
            if (tryPrimeMatch(market, itemsRepository, primeProcessedKey)) {
                return market;
            }

            // 尝试正则表达式匹配
            if (tryRegexMatch(market, itemsRepository, primeProcessedKey)) {
                return market;
            }

            // 所有匹配都失败，获取可能的物品列表
            market.setPossibleItems(getPossibleItems(itemsRepository, primeProcessedKey));
            return market;
        } catch (Exception e) {
            log.error("查询物品时发生异常: {}", e.getMessage(), e);
            market.setPossibleItems(getPossibleItems(getItemsRepository(), normalizeInput(key)));
            return market;
        }
    }

    /**
     * 标准化输入字符串
     */
    private static String normalizeInput(String key) {
        return key.toLowerCase(Locale.ROOT).replace("总图", "蓝图");
    }

    /**
     * 获取物品仓库实例
     */
    private static OrdersItemsRepository getItemsRepository() {
        return SpringUtils.getBean(OrdersItemsRepository.class);
    }

    /**
     * 获取别名仓库实例
     */
    private static AliasRepository getAliasRepository() {
        return SpringUtils.getBean(AliasRepository.class);
    }

    /**
     * 尝试直接匹配物品名称
     */
    private static boolean tryDirectMatch(Market market, OrdersItemsRepository itemsRepository, String key) {
        Optional<OrdersItems> items = itemsRepository.findByName(key);
        items.ifPresent(market::setItem);
        return items.isPresent();
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
     * 尝试通过别名匹配
     */
    private static boolean tryAliasMatch(Market market, OrdersItemsRepository itemsRepository, String key) {
        Optional<OrdersItems> items = itemsRepository.findByItemNameLike(key);
        items.ifPresent(market::setItem);
        return items.isPresent();
    }

    /**
     * 处理Prime关键词
     */
    private static String processPrimeKeyword(String key) {
        if (!key.contains("prime") && key.contains("p")) {
            return key.replace("p", "Prime");
        }
        return key;
    }

    /**
     * 尝试Prime关键词匹配
     */
    private static boolean tryPrimeMatch(Market market, OrdersItemsRepository itemsRepository, String key) {
        Optional<OrdersItems> items = itemsRepository.findByItemNameLike(key);
        items.ifPresent(market::setItem);
        return items.isPresent();
    }

    /**
     * 尝试正则表达式匹配 - 优先使用最长前缀
     */
    private static boolean tryRegexMatch(Market market, OrdersItemsRepository itemsRepository, String key) {
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
            log.debug("正则查询参数 - 前缀长度{}: '{}' -- 结尾: '{}'", prefixLength, header, end);

            Optional<OrdersItems> items = itemsRepository.findByItemNameRegex("^" + header + ".*?" + end + ".*?");
            if (items.isPresent()) {
                market.setItem(items.get());
                return true;
            }
        }

        return false;
    }

    /**
     * 获取可能的物品列表
     *
     * @param itemsRepository 物品仓库
     * @param key             查询关键字
     * @return 可能的物品列表
     */
    private static List<String> getPossibleItems(OrdersItemsRepository itemsRepository, String key) {
        //查询用户可能想要查询的物品
        List<OrdersItems> list = itemsRepository.findByItemNameLikeToList(String.valueOf(key.charAt(0)));
        //判断集合是否为空
        if (CollectionUtils.isEmpty(list)) {
            //根据别名模糊查询用户 可能想要查询的物品名称
            list = itemsRepository.findByItemNameLikeToList(StringUtils.substringBefore(key, String.valueOf(key.charAt(key.length() - 1))));
        }
        if (list.size() > 15) {
            list = list.subList(0, 15);
        }

        List<String> item = new ArrayList<>();
        for (OrdersItems o : list) {
            item.add(o.getName());
        }
        return item;
    }

    public static Market toSet(String key, MarketPlatformEnum form) {
        Market dataBase = toDataBase(key);
        if (dataBase.getPossibleItems() != null && !dataBase.getPossibleItems().isEmpty()) {
            return dataBase;
        }
        log.debug("查询参数 Key:{}", dataBase.getItem().getSlug());
        HttpUtils.Body body = ApiUrl.marketOrdersSet(dataBase.getItem().getSlug(), form);
        log.debug("查询结果:{}", body.body());
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                JsonNode itemsNode = dataNode.get("items");

                for (JsonNode itemNode : itemsNode) {
                    OrdersItems item = objectMapper.treeToValue(itemNode, OrdersItems.class);
                    if (dataBase.getItem().getId().equals(item.getId())) {
                        OrdersItems dbItem = dataBase.getItem();
                        dbItem.setReqMasteryRank(item.getReqMasteryRank());
                        dbItem.setTradingTax(item.getTradingTax());
                        dataBase.setItem(dbItem);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("解析Market数据失败: {}", e.getMessage());
                throw new RuntimeException("解析Market数据失败", e);
            }
        } else {
            throw new RuntimeException("触发速率限制，请稍后再次尝试查询。");
        }
        return dataBase;
    }


    /**
     * 查询Warframe.Market物品 并处理结果
     *
     * @param from   平台
     * @param isBy   是否是买家
     * @param isMax  是否是最大值
     * @param market 查询到的物品
     * @return 处理后的结果
     */
    public static BaseOrder<OrderWithUser> market(MarketPlatformEnum from, Boolean isBy, Boolean isMax, Market market) {
        String key = market.getItem().getSlug();
        log.debug("查询参数 From:{}  Key:{}  isBy:{}  isMax:{}", from, key, isBy, isMax);
        HttpUtils.Body body = ApiUrl.marketOrders(key, from);
        BaseOrder<OrderWithUser> owu = new BaseOrder<>();
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");

                List<OrderWithUser> list = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    OrderWithUser item = objectMapper.treeToValue(itemNode, OrderWithUser.class);
                    list.add(item);
                }

                list = list.stream()
                        // 筛选离线用户
                        .filter(o -> !o.getUser().getStatus().equals(MarketStatusEnum.OFFLINE))
                        // 筛选物品
                        .filter(o -> o.getItemId().equals(market.getItem().getId()))
                        // 筛选类型
                        .filter(o -> isBy ? o.getType().equals(TransactionEnum.BUY) : o.getType().equals(TransactionEnum.SELL))
                        // 筛选等级
                        .filter(o -> {
                            if (isMax) {
                                // 筛选Mod等级
                                if (o.getRank() != null) {
                                    return o.getRank().equals(market.getItem().getMaxRank());
                                }
                                // 筛选 阿耶檀识 星星数量
                                if (o.getAmberStars() != null && o.getCyanStars() != null) {
                                    return o.getAmberStars().equals(market.getItem().getMaxRank()) && o.getCyanStars().equals(market.getItem().getMaxRank());
                                }
                            }
                            return true;
                        })
                        // 排序物品
                        .sorted(isBy ? Comparator.comparing(OrderWithUser::getPlatinum).reversed() : Comparator.comparing(OrderWithUser::getPlatinum))
                        // 限制数量
                        .limit(8)
                        // 转换为结果
                        .toList();
                owu.setData(list);
                owu.setApiVersion(rootNode.get("apiVersion").asText());
                if (rootNode.has("error")) {
                    owu.setError(rootNode.get("error").asText());
                }
                return owu;
            } catch (Exception e) {
                log.error("解析Market订单数据失败, platform: {}, Slug: {}, ItemId: {}", from.getPlatform(), market.getItem().getSlug(), market.getItem().getId(), e);
                owu.setError("解析数据失败");
                return owu;
            }
        }
        if (body.code().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
            owu.setError("触发速率限制，请稍后再次尝试查询。");
            return owu;
        }
        owu.setError("查询出现错误");
        return owu;
    }



    @Data
    @Accessors(chain = true)
    public static class Market {
        OrdersItems item;
        List<String> possibleItems;
    }
}
