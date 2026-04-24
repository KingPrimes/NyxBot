package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@Slf4j
@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@ActiveProfiles("test")
public class TestMarketOrder {

    @Autowired
    MarketOrderUtils mou;

    @Test
    void testToDataBase() {
        List<String> keys = List.of(
                "阿利乌双枪p枪机",
                "利乌机",
                "阿利乌机",
                "阿利乌双枪 机",
                "阿利乌双枪p枪机",
                "阿p机",
                "猫甲",
                "猫甲机",
                "猫甲头",
                "猫甲系"
        );

        keys.forEach(key -> {
            MarketResult<OrdersItems, ?> set = mou.toDataBase(key);
            log.info("Key:{},Set:{}",key,set.toString());
        });
    }
}
