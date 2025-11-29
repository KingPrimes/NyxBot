package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.MarketResult;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MarketOrderUtils {
    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    /**
     * 从数据库中查询可能的值
     *
     * @param key 物品名称
     * @return 处理后的结果
     */
    private static MarketResult<OrdersItems, ?> toDataBase(String key) {
        MarketResult<OrdersItems, ?> market = new MarketResult<>();
        try {
            // 标准化输入
            String normalizedKey = MarketCommonUtils.normalizeInput(key);

            // 获取仓库实例
            OrdersItemsRepository itemsRepository = SpringUtils.getBean(OrdersItemsRepository.class);
            AliasRepository aliasRepository = SpringUtils.getBean(AliasRepository.class);

            // 尝试直接匹配物品名称
            if (MarketCommonUtils.tryMatch(market, normalizedKey, itemsRepository::findByName)) {
                return market;
            }

            String aliasProcessedKey = MarketCommonUtils.processAliases(normalizedKey, aliasRepository);
            // 尝试通过别名匹配
            if (MarketCommonUtils.tryMatch(market, aliasProcessedKey, itemsRepository::findByItemNameLike)) {
                return market;
            }

            String primeProcessedKey = MarketCommonUtils.processPrimeKeyword(aliasProcessedKey);
            // 尝试Prime关键词匹配
            if (MarketCommonUtils.tryMatch(market, primeProcessedKey, itemsRepository::findByItemNameLike)) {
                return market;
            }

            // 尝试正则表达式匹配
            if (MarketCommonUtils.tryRegexNameMatch(market, primeProcessedKey, itemsRepository)) {
                return market;
            }

            // 所有匹配都失败，获取可能的物品列表
            market.setPossibleItems(getPossibleItems(itemsRepository, primeProcessedKey));
            return market;
        } catch (Exception e) {
            log.error("查询物品时发生异常: {}", e.getMessage(), e);
            market.setPossibleItems(getPossibleItems(SpringUtils.getBean(OrdersItemsRepository.class), MarketCommonUtils.normalizeInput(key)));
            return market;
        }
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
        return list.stream()
                .limit(15)
                .map(OrdersItems::getName)
                .toList();
    }

    /**
     * 检查订单是否匹配最大等级条件
     * <p>根据是否需要最大等级的条件，验证订单是否符合条件。
     * 对于Mod物品检查等级，对于阿耶檀识之星物品检查星级。</p>
     *
     * @param isMax  是否需要最大等级/星级
     * @param market 市场信息，包含物品的最大等级信息
     * @param o      订单信息，包含物品等级或星级
     * @return 如果订单满足最大等级条件返回true，否则返回false
     */
    private static boolean matchesMaxRank(boolean isMax, MarketResult<OrdersItems, ?> market, OrderWithUser o) {
        if (!isMax) {
            return true;
        }
        Integer maxRank = market.getEntity().getMaxRank();
        if (o.getRank() != null) {
            return o.getRank().equals(maxRank);
        }
        if (o.getAmberStars() != null && o.getCyanStars() != null) {
            return o.getAmberStars().equals(maxRank) && o.getCyanStars().equals(maxRank);
        }
        return true;
    }

    /**
     * 根据关键字和平台获取市场物品信息
     * <p>首先从本地数据库查询物品信息，如果未找到确切匹配项，则通过API查询网络数据补充信息</p>
     *
     * @param key  物品关键字
     * @param form 平台枚举
     * @return Market对象，包含物品信息或可能的物品列表
     */
    public static MarketResult<OrdersItems, ?> toSet(String key, MarketPlatformEnum form) {
        MarketResult<OrdersItems, ?> dataBase = toDataBase(key);
        if (dataBase.getPossibleItems() != null && !dataBase.getPossibleItems().isEmpty()) {
            return dataBase;
        }
        log.debug("查询参数 Key:{}", dataBase.getEntity().getSlug());
        HttpUtils.Body body = ApiUrl.marketOrdersSet(dataBase.getEntity().getSlug(), form);
        log.debug("查询结果:{}", body.body());
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                JsonNode itemsNode = dataNode.get("items");

                for (JsonNode itemNode : itemsNode) {
                    OrdersItems item = objectMapper.treeToValue(itemNode, OrdersItems.class);
                    if (dataBase.getEntity().getId().equals(item.getId())) {
                        OrdersItems dbItem = dataBase.getEntity();
                        dbItem.setReqMasteryRank(item.getReqMasteryRank());
                        dbItem.setTradingTax(item.getTradingTax());
                        dataBase.setEntity(dbItem);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("解析Market数据失败", e);
            }
        } else if (body.code().equals(HttpStatus.TOO_MANY_REQUESTS)) {
            throw new RuntimeException("触发速率限制，请稍后再次尝试查询。");
        } else {
            throw new RuntimeException("查询Market数据失败, Code:%d Headers:%s".formatted(body.code().value(), body.headers().toSingleValueMap().toString()));
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
    public static BaseOrder<OrderWithUser> market(MarketPlatformEnum from, Boolean isBy, Boolean isMax, MarketResult<OrdersItems, ?> market) {
        String key = market.getEntity().getSlug();
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
                        .filter(o -> o.getItemId().equals(market.getEntity().getId()))
                        // 筛选类型
                        .filter(o -> isBy ? o.getType().equals(TransactionEnum.BUY) : o.getType().equals(TransactionEnum.SELL))
                        // 筛选等级
                        .filter(o -> matchesMaxRank(isMax, market, o))
                        // 排序物品
                        .sorted(isBy
                                ? Comparator.comparing(OrderWithUser::getPlatinum).reversed()
                                : Comparator.comparing(OrderWithUser::getPlatinum))
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
                log.error("解析Market订单数据失败, platform: {}, Slug: {}, ItemId: {}", from.getPlatform(), market.getEntity().getSlug(), market.getEntity().getId(), e);
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
}
