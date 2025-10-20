package com.nyx.bot.models.image;

import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.res.market.BaseOrder;
import com.nyx.bot.modules.warframe.res.market.OrderWithUser;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.HtmlToImage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ModelMap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestMarketOrdersForImage {

    private static final String TEST_IMAGE_PATH = "./data/market_orders.png";
    String form = "pc";
    String key = "nova";
    Boolean isBy = false;
    Boolean isMax = false;

    @Test
    void test() throws DataNotInfoException, HtmlToImageException, IOException {
        byte[] bytes = postMarketOrdersImage();
        assertNotNull(bytes, "生成的图片字节数据为空");
        assertTrue(bytes.length > 0, "生成的图片字节数据长度为0");
        try (FileOutputStream fos = new FileOutputStream(TEST_IMAGE_PATH)) {
            fos.write(bytes);
        }
    }

    private byte[] postMarketOrdersImage() throws DataNotInfoException, HtmlToImageException {
        MarketUtils.Market market = MarketUtils.toSet(key, form);
        ModelMap modelMap = new ModelMap();
        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            modelMap.put("items", market.getPossibleItems());
            return HtmlToImage.generateImage("html/marketPossibleItems", () -> modelMap).toByteArray();
        }
        BaseOrder<OrderWithUser> order = MarketUtils.market(key, isBy, isMax, market);
        List<OrderWithUser> ows = order.getData();
        OrdersItems oi = market.getItem();
        modelMap.addAttribute("ducats", oi.getDucats());
        modelMap.addAttribute("level", oi.getReqMasteryRank());
        modelMap.addAttribute("credits", oi.getTradingTax());
        modelMap.addAttribute("itemName", market.getItem().getName());
        modelMap.addAttribute("form", form);
        modelMap.addAttribute("isBy", isBy);
        modelMap.addAttribute("isMax", isMax);
        modelMap.addAttribute("orders", ows);

        return HtmlToImage.generateImage("html/market", () -> modelMap).toByteArray();
    }
}
