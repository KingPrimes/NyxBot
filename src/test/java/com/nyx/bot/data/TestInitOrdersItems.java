package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Objects;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Rollback(false)
@Slf4j
public class TestInitOrdersItems {

    @Resource
    OrdersItemsRepository ordersItemsService;

    @Test
    @Transactional
    void getOrdersItems() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_ITEMS);
        if (body.getCode() == HttpCodeEnum.SUCCESS) {
            JSONArray data = JSON.parseObject(body.getBody()).getJSONArray("data");
            if (data.isEmpty()) {
                log.info("未获取到Market物品数据");
                return;
            }
            // 2. 使用Stream流处理集合，代码更简洁
            List<OrdersItems> items = data.stream()
                    .map(i->(JSONObject)i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildOrdersItems) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
            ordersItemsService.saveAll(items);
        }
    }

    // 3. 辅助方法：提取OrdersItems构建逻辑，集中处理JSON解析和空值安全
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

            return new OrdersItems()
                    .setGameRef(gameRef)
                    .setId(id)
                    .setSlug(slug)
                    .setBulkTradable(object.getBooleanValue("bulkTradable")) // 布尔值默认false
                    .setMaxRank(object.getInteger("maxRank"))
                    .setDucats(object.getInteger("ducats"))
                    .setName(name)
                    .setIcon(object.getString("icon"))
                    .setThumb(object.getString("thumb"));
        } catch (JSONException e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
