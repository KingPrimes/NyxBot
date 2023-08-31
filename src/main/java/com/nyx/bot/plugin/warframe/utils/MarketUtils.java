package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.AliasRepository;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
import com.nyx.bot.res.MarketOrders;
import com.nyx.bot.utils.HttpUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


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
            OrdersItems items;
            //假设用户使用了别名
            //匹配是否使用了别名 查出所有的别名列表并迭代查询
            List<Alias> aliases = aliasRepository.findAll();

            AtomicReference<String> finalKey = new AtomicReference<>(key);
            //判断 是否使用了别名
            aliases.stream().filter(a -> finalKey.get().contains(a.getCn())).findFirst().ifPresent(a -> finalKey.set(finalKey.get().replace(a.getCn(), a.getEn())));
            key = finalKey.get();

            //直接使用别名模糊查询
            items = itemsRepository.findByItemNameLike(key);
            if (items != null) {
                market.setItemName(items.getItemName());
                market.setKey(items.getUrlName());
                return market;
            }

            String header = key.substring(0, key.length() - 1);

            String end = key.substring(key.length() - 1);
            //正则查询
            items = itemsRepository.findByItemNameRegex("^" + header + ".*" + end);
            if (items != null) {
                market.setItemName(items.getItemName());
                market.setKey(items.getUrlName());
                return market;

            } else {
                header = key.substring(0, key.length() - 2);
                end = key.substring(key.length() - 2);
                //正则查询
                items = itemsRepository.findByItemNameRegex("^" + header + ".*" + end);
                if (items != null) {
                    market.setItemName(items.getItemName());
                    market.setKey(items.getUrlName());
                    return market;
                }
            }

            if (!key.contains("prime") && key.contains("p")) {
                key = key.replace("p", "Prime");
            }

            items = itemsRepository.findByItemNameLike(key);
            market.setKey(items.getUrlName());
            market.setItemName(items.getItemName());
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
                item.add(o.getItemName());
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

        orders = JSONObject.parseObject(body.getBody(), MarketOrders.class);

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

    private static List<MarketOrders.Orders> orders(MarketOrders order, Boolean isBy, Boolean isMax) {
        List<MarketOrders.Orders> list;
        AtomicInteger max = new AtomicInteger();
        boolean flag = order.getPayload().getOrders().get(0).getModRank() != null;

        if (isMax && flag) {
            order.getPayload().getOrders().forEach(orders -> {
                if (max.get() < orders.getModRank()) {
                    max.set(orders.getModRank());
                }
            });
        }

        list = isByOrSell(order, isBy, isMax, flag, max.get());

        if (!isBy) {
            list.sort(Comparator.comparingInt(MarketOrders.Orders::getPlatinum));
        } else {
            list.sort((o1, o2) -> o2.getPlatinum().compareTo(o1.getPlatinum()));
        }

        if (list.size() > 8) {
            list = list.subList(0, 7);
        }

        return list;


    }

    private static List<MarketOrders.Orders> isByOrSell(MarketOrders order, Boolean isBy, Boolean isMax, Boolean flag, Integer max) {
        List<MarketOrders.Orders> list = new ArrayList<>();
        if (isBy) {
            if (isMax && flag) {
                order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是购买订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("buy") && Objects.equals(orders.getModRank(), max))
                        .forEach(list::add);
            } else {
                order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是购买订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("buy"))
                        .forEach(list::add);
            }
        } else {
            if (isMax && flag) {
                order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是出售订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("sell") && Objects.equals(orders.getModRank(), max))
                        .forEach(list::add);
            } else {
                order.getPayload().getOrders()
                        .stream()
                        //过滤 寻找所有非不在线玩家，并是出售订单，并且是满级的物品
                        .filter(orders -> !orders.getUser().getStatus().equals("offline") && orders.getOrderType().equals("sell"))
                        .forEach(list::add);
            }
        }
        return list;
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
