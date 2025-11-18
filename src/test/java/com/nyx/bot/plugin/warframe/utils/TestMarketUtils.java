package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.data.Constant;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.res.Ducats;
import com.nyx.bot.modules.warframe.res.MarketRiven;
import com.nyx.bot.modules.warframe.res.market.BaseOrder;
import com.nyx.bot.modules.warframe.res.market.OrderWithUser;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.HtmlToImage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ModelMap;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestMarketUtils {

    /**
     * 测试查询紫卡所需的时间
     */
    @Test
    public void testMarketRiven() throws DataNotInfoException, HtmlToImageException, IOException {
        // 记录执行时间
        long start = System.currentTimeMillis();
        // 调用接口
        MarketRiven marketRiven = MarketUtils.marketRivenParameter("绝路");


        byte[] byteArray = HtmlToImage.generateImage("html/marketRiven", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("riven", marketRiven);
            return modelMap;
        }).toByteArray();
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(byteArray)), Constant.PNG, new File(Constant.DRAW_PATH.formatted("draw_market_riven.png")));

        // 记录执行时间
        long end = System.currentTimeMillis();
        // 打印结果
        log.info("执行时间：{}\n,查询marketRiven:{}", end - start, JSON.toJSONString(marketRiven));
    }

    @Test
    void testMarketOrders() throws DataNotInfoException, HtmlToImageException, IOException {
        MarketFormEnums form = MarketFormEnums.PC;
        String key = "nova";
        MarketUtils.Market market = MarketUtils.toSet(key, form);
        log.info("market: {}", JSON.toJSONString(market));
        BaseOrder<OrderWithUser> order = MarketUtils.market(form, false, false, market);
        log.info("order: {}", JSON.toJSONString(order));
        ModelMap modelMap = new ModelMap();
        List<OrderWithUser> ows = order.getData();
        OrdersItems oi = market.getItem();
        modelMap.addAttribute("ducats", oi.getDucats());
        modelMap.addAttribute("level", oi.getReqMasteryRank());
        modelMap.addAttribute("credits", oi.getTradingTax());
        modelMap.addAttribute("itemName", market.getItem().getName());
        modelMap.addAttribute("form", "PC");
        modelMap.addAttribute("isBy", false);
        modelMap.addAttribute("isMax", false);
        modelMap.addAttribute("orders", ows);
        byte[] byteArray = HtmlToImage.generateImage("html/market", () -> modelMap).toByteArray();
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(byteArray)), Constant.PNG, new File(Constant.DRAW_PATH.formatted("draw_market_orders.png")));
    }

    @Test
    void testMarketDucats() {
        Ducats ducats = MarketUtils.getDucats();
        Map<String, List<Ducats.Ducat>> godDump = Objects.requireNonNull(ducats).getPayload().getGodDump();
        log.info("godDump: {}", JSON.toJSONString(godDump));
        Map<String, List<Ducats.Ducat>> silverDump = ducats.getPayload().getSilverDump();
        log.info("silverDump: {}", JSON.toJSONString(silverDump));
    }

}
