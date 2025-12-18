package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrdersItemsService {
    
    ObjectMapper objectMapper;
    
    OrdersItemsRepository ordersItemsService;

    public OrdersItemsService(ObjectMapper objectMapper,OrdersItemsRepository ordersItemsService) {
        this.objectMapper = objectMapper;
        this.ordersItemsService = ordersItemsService;
    }

    @Transactional
    public Integer initOrdersItemsData() {
        log.info("开始初始化Market物品数据……");
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_ITEMS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.error("未获取到Market物品数据");
                    return -1;
                }
                List<OrdersItems> items = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    OrdersItems item = buildOrdersItems(itemNode);
                    if (item != null) {
                        items.add(item);
                    }
                }
                log.info("成功初始化Market物品数据，数量为：{}", items.size());
                return ordersItemsService.saveAllAndFlush(items).size();
            } catch (Exception e) {
                log.error("解析Market物品数据失败", e);
                return -1;
            }
        }
        return -1;
    }

    private OrdersItems buildOrdersItems(JsonNode object) {
        try {
            String gameRef = object.has("gameRef") ? object.get("gameRef").asText() : null;
            String id = object.has("id") ? object.get("id").asText() : null;
            String slug = object.has("slug") ? object.get("slug").asText() : null;
            if (StringUtils.isAnyBlank(id, slug)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JsonNode i18nNode = object.has("i18n") ? object.get("i18n") : null;
            JsonNode zhHansNode = (i18nNode != null && i18nNode.has("zh-hans")) ? i18nNode.get("zh-hans") : null;
            String name = (zhHansNode != null && zhHansNode.has("name")) ? zhHansNode.get("name").asText() : slug;
            String icon = (zhHansNode != null && zhHansNode.has("icon")) ? zhHansNode.get("icon").asText() : null;
            String thumb = (zhHansNode != null && zhHansNode.has("thumb")) ? zhHansNode.get("thumb").asText() : null;

            return new OrdersItems()
                    .setGameRef(gameRef)
                    .setId(id)
                    .setSlug(slug)
                    .setBulkTradable(object.has("bulkTradable") && object.get("bulkTradable").asBoolean())
                    .setMaxRank(object.has("maxRank") ? object.get("maxRank").asInt() : null)
                    .setDucats(object.has("ducats") ? object.get("ducats").asInt() : null)
                    .setVaulted(object.has("vaulted") && object.get("vaulted").asBoolean())
                    .setMaxAmberStars(object.has("maxAmberStars") ? object.get("maxAmberStars").asInt() : null)
                    .setMaxCyanStars(object.has("maxCyanStars") ? object.get("maxCyanStars").asInt() : null)
                    .setBaseEndo(object.has("baseEndo") ? object.get("baseEndo").asInt() : null)
                    .setName(name)
                    .setIcon(icon)
                    .setThumb(thumb);
        } catch (Exception e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
