package com.nyx.bot.data;

import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.modules.warframe.service.RivenItemsService;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Rollback(false)
@Slf4j
public class TestInitMarketRiven {
    @Test
    void testInitMarketRiven() {
        Integer i = SpringUtils.getBean(RivenItemsService.class).initRivenItemsData();
        log.info("总计更新 Warframe.MarketRiven {} 数据！", i);
    }
}
