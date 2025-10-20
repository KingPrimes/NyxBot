package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.res.MarketRiven;
import com.nyx.bot.modules.warframe.res.market.BaseOrder;
import com.nyx.bot.modules.warframe.res.market.OrderWithUser;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestMarketUtils {

    /**
     * 测试查询紫卡所需的时间
     */
    @Test
    public void testMarketRiven() {
        // 记录执行时间
        long start = System.currentTimeMillis();
        // 调用接口
        MarketRiven marketRiven = MarketUtils.marketRivenParameter("绝路");
        // 记录执行时间
        long end = System.currentTimeMillis();
        // 打印结果
        log.info("执行时间：{}\n,查询marketRiven:{}", end - start, JSON.toJSONString(marketRiven));
    }

    @Test
    void testMarketOrders() {
        String form = "pc";
        String key = "nova";
        MarketUtils.Market market = MarketUtils.toSet(key, form);
        log.info("market: {}", JSON.toJSONString(market));
        BaseOrder<OrderWithUser> order = MarketUtils.market(form, true, false, market);
        log.info("order: {}", JSON.toJSONString(order));
    }

}
