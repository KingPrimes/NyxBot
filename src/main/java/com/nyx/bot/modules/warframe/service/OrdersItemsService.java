package com.nyx.bot.modules.warframe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrdersItemsService {
    OrdersItemsRepository ordersItemsService;

    public OrdersItemsService(OrdersItemsRepository ordersItemsService) {
        this.ordersItemsService = ordersItemsService;
    }

    @Transactional
    public Integer initOrdersItemsData() {
        log.debug("开始初始化Market物品数据……");
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_ITEMS);
        if (body.getCode() == HttpCodeEnum.SUCCESS) {
            JSONArray data = JSON.parseObject(body.getBody()).getJSONArray("data");
            if (data.isEmpty()) {
                log.error("未获取到Market物品数据");
                return -1;
            }
            // 2. 使用Stream流处理集合，代码更简洁
            List<OrdersItems> items = data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildOrdersItems) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
            log.debug("成功初始化Market物品数据，数量为：{}", items.size());
            return ordersItemsService.saveAll(items).size();
        }
        return -1;
    }

    private OrdersItems buildOrdersItems(JSONObject object) {
        try {
            String gameRef = object.getString("gameRef");
            String id = object.getString("id");
            String slug = object.getString("slug");
            if (StringUtils.isAnyBlank(id, slug)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JSONObject i18n = object.getJSONObject("i18n");
            JSONObject zhHansI18n = i18n != null ? i18n.getJSONObject("zh-hans") : null;
            String name = zhHansI18n != null ? zhHansI18n.getString("name") : object.getString("slug"); // 名称缺失时用slug兜底
            String icon = zhHansI18n != null ? zhHansI18n.getString("icon") : null;
            String thumb = zhHansI18n != null ? zhHansI18n.getString("thumb") : null;

            return new OrdersItems()
                    .setGameRef(gameRef)
                    .setId(id)
                    .setSlug(slug)
                    .setBulkTradable(object.getBooleanValue("bulkTradable")) // 布尔值默认false
                    .setMaxRank(object.getInteger("maxRank"))
                    .setDucats(object.getInteger("ducats"))
                    .setVaulted(object.getBooleanValue("vaulted"))
                    .setMaxAmberStars(object.getInteger("maxAmberStars"))
                    .setMaxCyanStars(object.getInteger("maxCyanStars"))
                    .setBaseEndo(object.getInteger("baseEndo"))
                    .setName(name)
                    .setIcon(icon)
                    .setThumb(thumb);
        } catch (JSONException e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
