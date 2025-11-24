package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.modules.warframe.entity.*;
import com.nyx.bot.modules.warframe.repo.*;
import com.nyx.bot.modules.warframe.resp.MarketRivenParameter;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.market.MarketRiven;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class MarketRivenUtils {

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);


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
        items.get().setName(key);
        items = repository.findByName(key);
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

    public static MarketRiven marketRivenParameter(String key) {
        log.debug("------紫卡查询--构建请求地址------");
        MarketRiven marketRiven = new MarketRiven();
        RivenItems riveItems;
        if (!key.contains("-")) {
            log.debug("------无词条参数状态------");
            riveItems = getRiveItems(key.trim());
            // 判断是否有查询到物品,如果没有则返回可能要查询的物品列表
            if (riveItems.getItems() != null) {
                //marketRiven.setPossibleItems(riveItems.getItems());
                log.debug("------未查询到匹配的物品------");
                return marketRiven;
            }
            // 构建请求url
            String url = new MarketRivenParameter(
                    riveItems.getSlug(),
                    riveItems.getName(),
                    "",
                    "",
                    MarketRivenParameter.Polarity.ANY,
                    7,
                    16,
                    MarketRivenParameter.Policy.ANY,
                    MarketRivenParameter.SortBy.PRICE_ASC
            ).getUrl();
            log.debug("请求的MarketRiven无参 URL:{}", url);
            HttpUtils.Body body = HttpUtils.marketSendGet(url);
            if (!body.code().is2xxSuccessful()) {
                log.debug("获取数据失败：{}", body.body());
                return marketRiven;
            }
            try {
                var mr = stream(objectMapper.readValue(body.body(), MarketRiven.class), riveItems.getName());
                log.debug("获取到的数据：\n{}", mr);
                // 解析返回数据
                return mr;
            } catch (Exception e) {
                log.error("解析紫卡数据失败: {}", e.getMessage());
                return marketRiven;
            }
        }
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
                riveItems.getSlug(),
                riveItems.getName(),
                statBuilder.toString(),
                not,
                MarketRivenParameter.Polarity.ANY,
                7,
                16,
                MarketRivenParameter.Policy.ANY,
                MarketRivenParameter.SortBy.PRICE_ASC
        ).getUrl();
        log.debug("有词条参数 请求的MarketRiven带参 URL:{}", url);
        HttpUtils.Body body = HttpUtils.marketSendGet(url);

        if (!body.code().is2xxSuccessful()) {
            log.debug("有词条参数 获取数据失败：{}", body.body());
            return marketRiven;
        }
        try {
            var mr = stream(objectMapper.readValue(body.body(), MarketRiven.class), riveItems.getName());
            log.debug("有词条参数 获取到的数据：\n{}", mr);
            // 解析返回数据
            return mr;
        } catch (Exception e) {
            log.error("有词条参数 解析紫卡数据失败: {}", e.getMessage());
            return marketRiven;
        }
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




}
