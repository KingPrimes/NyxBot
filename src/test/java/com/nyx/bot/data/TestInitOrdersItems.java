package com.nyx.bot.data;

import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.modules.warframe.service.OrdersItemsService;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Rollback(false)
@Slf4j
public class TestInitOrdersItems {


    @Test
    void initOrdersItems() {
        Integer i = SpringUtils.getBean(OrdersItemsService.class).initOrdersItemsData();
        log.info("总计更新 Warframe.OrdersItems {} 数据！", i);
    }
}
