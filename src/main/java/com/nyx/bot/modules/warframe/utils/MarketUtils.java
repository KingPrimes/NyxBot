package com.nyx.bot.modules.warframe.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.entity.*;
import com.nyx.bot.modules.warframe.repo.*;
import com.nyx.bot.modules.warframe.res.Ducats;
import com.nyx.bot.modules.warframe.res.MarketOrders;
import com.nyx.bot.modules.warframe.res.MarketRiven;
import com.nyx.bot.modules.warframe.resp.MarketRivenParameter;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class MarketUtils {

    /**
     * 从数据库中查询可能的值
     *
     * @param key 物品名称
     * @return 处理后的结果
     */
    public static Market to(String key) {
        Market market = new Market();

        AliasRepository aliasRepository = SpringUtils.getBean(AliasRepository.class);

        OrdersItemsRepository itemsRepository = SpringUtils.getBean(OrdersItemsRepository.class);

        key = key.toLowerCase(Locale.ROOT).replace("总图", "蓝图");

        try {
            Optional<OrdersItems> items;
            //假设用户使用了别名
            //匹配是否使用了别名 查出所有的别名列表并迭代查询
            List<Alias> aliases = aliasRepository.findAll();

            AtomicReference<String> finalKey = new AtomicReference<>(key);
            //判断 是否使用了别名
            aliases.stream().filter(a -> finalKey.get().contains(a.getCn())).findFirst().ifPresent(a -> finalKey.set(finalKey.get().replace(a.getCn(), a.getEn())));
            key = finalKey.get();

            //直接使用别名模糊查询
            items = itemsRepository.findByItemNameLike(key);
            if (items.isPresent()) {
                market.setItemName(items.get().getName());
                market.setKey(items.get().getSlug());
                return market;
            }

            if (!key.contains("prime") && key.contains("p")) {
                key = key.replace("p", "Prime");
            }

            items = itemsRepository.findByItemNameLike(key);
            if (items.isPresent()) {
                market.setKey(items.get().getSlug());
                market.setItemName(items.get().getName());
                return market;
            }

            String header = key.substring(0, key.length() - 1);

            String end = key.substring(key.length() - 1);
            //正则查询
            items = itemsRepository.findByItemNameRegex("^" + header + ".*" + end);
            if (items.isPresent()) {
                market.setItemName(items.get().getName());
                market.setKey(items.get().getSlug());
                return market;

            } else {
                header = key.substring(0, key.length() - 2);
                end = key.substring(key.length() - 2);
                //正则查询
                items = itemsRepository.findByItemNameRegex("^" + header + ".*" + end);
                if (items.isPresent()) {
                    market.setItemName(items.get().getName());
                    market.setKey(items.get().getSlug());
                    return market;
                }
            }

            return market;
        } catch (Exception e) {
            //查询用户可能想要查询的物品
            List<OrdersItems> items = itemsRepository.findByItemNameLikeToList(String.valueOf(key.charAt(0)));
            //判断集合是否为空
            if (CollectionUtils.isEmpty(items)) {
                //根据别名模糊查询用户 可能想要查询的物品名称
                items = itemsRepository.findByItemNameLikeToList(StringUtils.substringBefore(key, String.valueOf(key.charAt(key.length() - 1))));
            }

            if (items.size() > 15) {
                items = items.subList(0, 15);
            }

            List<String> item = new ArrayList<>();
            for (OrdersItems o : items) {
                item.add(o.getName());
            }

            market.setPossibleItems(item);
            return market;
        }
    }


    /**
     * 查询Warframe.Market物品 并处理结果
     *
     * @param from  平台
     * @param key   url_name
     * @param isBy  是否是买家
     * @param isMax 是否是最大值
     * @return 处理后的结果
     */
    public static MarketOrders market(String from, String key, Boolean isBy, Boolean isMax) {
        HttpUtils.Body body = ApiUrl.marketOrders(key, from);
        MarketOrders orders = new MarketOrders();
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            orders.setCode(body.getCode().name());
            return orders;
        }

        orders = JSONObject.parseObject(body.getBody(), MarketOrders.class, JSONReader.Feature.SupportSmartMatch);

        orders.setCode(body.getCode().name());

        orders.getPayload().setOrders(orders(orders, isBy, isMax));

        List<MarketOrders.ItemsInSet> itemsInSets = new ArrayList<>();

        orders.getInclude().getItem().getItemsInSet()
                .stream()
                .filter(item -> item.getUrlName().equals(key))
                .findFirst()
                .ifPresent(itemsInSets::add);

        orders.getInclude().getItem().setItemsInSet(itemsInSets);

        return orders;
    }

    /**
     * 排序查询结果
     *
     * @param order 元数据
     * @param isBy  买/卖
     * @param isMax 是否为满级
     * @return 排序后得结果
     */
    static List<MarketOrders.Orders> orders(MarketOrders order, Boolean isBy, Boolean isMax) {
        AtomicInteger max = new AtomicInteger();
        boolean flag = order.getPayload().getOrders().get(0).getModRank() != null;

        if (isMax && flag) {
            order.getPayload().getOrders().forEach(orders -> {
                if (max.get() < orders.getModRank()) {
                    max.set(orders.getModRank());
                }
            });
        }
        return isByOrSell(order, isBy, isMax, flag, max.get());
    }

    /**
     * 筛选买卖是否为满级
     *
     * @param order 元数据
     * @param isBy  买卖
     * @param isMax 是否为满级
     * @param flag  是否查询满级
     * @param max   MOD得最大等级
     */
    static List<MarketOrders.Orders> isByOrSell(MarketOrders order, Boolean isBy, Boolean isMax, Boolean flag, Integer max) {
        if (isBy) {
            if (isMax && flag) {
                return order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是购买订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("buy") && Objects.equals(orders.getModRank(), max))
                        .sorted((o1, o2) -> o2.getPlatinum().compareTo(o1.getPlatinum()))
                        .limit(8)
                        .toList();
            } else {
                return order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是购买订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("buy"))
                        .sorted((o1, o2) -> o2.getPlatinum().compareTo(o1.getPlatinum()))
                        .limit(8)
                        .toList();
            }
        } else {
            if (isMax && flag) {
                return order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是出售订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("sell") && Objects.equals(orders.getModRank(), max))
                        .sorted(Comparator.comparingInt(MarketOrders.Orders::getPlatinum))
                        .limit(8)
                        .toList();
            } else {
                return order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是出售订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("sell"))
                        .sorted(Comparator.comparingInt(MarketOrders.Orders::getPlatinum))
                        .limit(8)
                        .toList();
            }
        }
    }

    /**
     * 获取紫卡武器信息 如果遇到为查询到的物品则返回可能要查询的物品列表
     * RivenItems.items 可能要查询的物品列表
     */
    static RivenItems getRiveItems(String key) {
        log.debug("------紫卡查询--查询数据库------");
        log.debug("原始数据：{}", key);
        // 假设用户输入的是正确值，直接查询数据库
        var repository = SpringUtils.getBean(RivenItemsRepository.class);
        Optional<RivenItems> items = Optional.of(new RivenItems());
        items.get().setItemName(key);
        items = repository.findByItemName(key);
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
            items = repository.findByItemName(a.get().getEn());
            log.debug("别名:{}", items);
            if (items.isPresent()) {
                return items.get();
            }
        }

        // 正则查询
        String start = String.valueOf(key.charAt(0));
        String end = String.valueOf(key.charAt(key.length() - 1));
        items = repository.findByItemNameRegex("^" + start + ".*?" + end + ".*?");
        log.debug("正则查询:{}", items);
        if (items.isPresent()) {
            return items.get();
        }

        //最后查询所有以该字符开头的物品，并返回
        List<RivenItems> itemsList = repository.itemNameLikes(start);
        RivenItems finalItems = new RivenItems();
        finalItems.setItems(itemsList);
        log.debug("可能存在的值：{}", finalItems);
        log.debug("------数据库查询结束------");
        return finalItems;
    }

    public static MarketRiven marketRivenParameter(String key) {
        log.debug("------紫卡查询--构建请求地址------");
        MarketRiven marketRiven = new MarketRiven();
        RivenItems riveItems;
        if (!key.contains("-")) {
            log.debug("------无词条参数状态------");
            riveItems = getRiveItems(key.trim());
            // 判断是否有查询到物品,如果没有则返回可能要查询的物品列表
            if (riveItems.getItems() != null) {
                marketRiven.setPossibleItems(riveItems.getItems());
                log.debug("------未查询到匹配的物品------");
                return marketRiven;
            }
            // 构建请求url
            String url = new MarketRivenParameter(
                    riveItems.getUrlName(),
                    riveItems.getItemName(),
                    "",
                    "",
                    MarketRivenParameter.Polarity.ANY,
                    7,
                    16,
                    MarketRivenParameter.Policy.ANY,
                    MarketRivenParameter.SortBy.PRICE_ASC
            ).getUrl();
            log.debug("请求的URL:{}", url);
            HttpUtils.Body body = HttpUtils.marketSendGet(url, "");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.debug("获取数据失败：{}", body.getCode().getMessage());
                return marketRiven;
            }
            var mr = stream(JSONObject.parseObject(body.getBody(), MarketRiven.class, JSONReader.Feature.SupportSmartMatch), riveItems.getItemName());
            log.debug("获取到的数据：\n{}", mr);
            // 解析返回数据
            return mr;
        }
        log.debug("------有词条参数状态------");
        // 有请求参数 以-分割
        List<String> list = Arrays.stream(key.split("-")).toList();
        riveItems = getRiveItems(list.get(0).trim());
        // 判断是否有查询到物品,如果没有则返回可能要查询的物品列表
        if (riveItems.getItems() != null) {
            log.debug("------未查询到匹配的物品------");
            marketRiven.setPossibleItems(riveItems.getItems());
            return marketRiven;
        }
        // 正面词条
        String[] stats = list.get(1).replaceAll("，", ",").split(",");
        // 正面词条 变量
        StringBuilder statBuilder = new StringBuilder();
        for (int i = 0; i < stats.length; i++) {
            Optional<RivenTion> tion = SpringUtils.getBean(RivenTionRepository.class).findByEffect(stats[i].trim());
            if (tion.isPresent()) {
                statBuilder.append(tion.get().getUrlName());
                // 判断后续还有没有词条，如果有则添加逗号
                if (i < stats.length - 1) {
                    statBuilder.append(",");
                }
            } else {
                Optional<RivenTionAlias> alias = SpringUtils.getBean(RivenTionAliasRepository.class).findByCn(stats[i].trim());
                if (alias.isPresent()) {
                    statBuilder.append(alias.get().getEn());
                    // 判断后续还有没有词条，如果有则添加逗号
                    if (i < stats.length - 1) {
                        statBuilder.append(",");
                    }
                }
            }
        }
        // 负面词条
        String not = "";
        if (list.size() > 2) {
            not = list.get(2).replace("有", "has").replace("无", "none");
        }
        log.debug("正面词条：{} ---- 负面词条:{}", statBuilder, not);
        // 构建请求url
        String url = new MarketRivenParameter(
                riveItems.getUrlName(),
                riveItems.getItemName(),
                statBuilder.toString(),
                not,
                MarketRivenParameter.Polarity.ANY,
                7,
                16,
                MarketRivenParameter.Policy.ANY,
                MarketRivenParameter.SortBy.PRICE_ASC
        ).getUrl();
        log.debug("请求的URL:{}", url);
        HttpUtils.Body body = HttpUtils.marketSendGet(url, "");

        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.debug("获取数据失败：{}", body.getCode().getMessage());
            return marketRiven;
        }
        var mr = stream(JSONObject.parseObject(body.getBody(), MarketRiven.class, JSONReader.Feature.SupportSmartMatch), riveItems.getItemName());
        log.debug("获取到的数据：\n{}", mr);
        // 解析返回数据
        return mr;
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
                        .filter(MarketRiven.Payload.Auctions::getVisible)
                        // 过滤掉不在线玩家
                        .filter(m -> m.getOwner().getStatus().equals("online") || m.getOwner().getStatus().equals("ingame"))
                        // 排序
                        .sorted((o1, o2) -> {
                            if (o1.getBuyoutPrice() != null && o2.getBuyoutPrice() != null) {
                                return o1.getBuyoutPrice() - o2.getBuyoutPrice();
                            }

                            if (o1.getStartingPrice() != null && o2.getStartingPrice() != null) {
                                return o1.getStartingPrice() - o2.getStartingPrice();
                            }

                            if (o1.getTopBid() != null && o2.getTopBid() != null) {
                                return o1.getTopBid() - o2.getTopBid();
                            }
                            return 0;
                        })
                        // 取前10个
                        .limit(10)
                        .peek(m -> m.getItem().setAttributes(m.getItem().getAttributes().stream().peek(a ->
                                a.setUrlName(SpringUtils.getBean(RivenTionRepository.class).findByUrlName(a.getUrlName()).orElse(new RivenTion()).getEffect())).toList()
                        ))
                        .toList());
        // 解析返回数据
        return marketRiven;
    }

    public static Ducats getDucats() {
        HttpUtils.Body body = HttpUtils.marketSendGet("https://api.warframe.market/v1/tools/ducats", "");
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.debug("获取数据失败：{}", body.getCode().getMessage());
            return null;
        }
        return JSONObject.parseObject(body.getBody(), Ducats.class, JSONReader.Feature.SupportSmartMatch);
    }

    @Data
    public static class Market {
        String key;
        String itemName;
        List<String> possibleItems;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("key", key)
                    .append("itemName", itemName)
                    .append("possibleItems", possibleItems)
                    .toString();
        }
    }

}
