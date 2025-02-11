package com.nyx.bot.data;

import com.nyx.bot.NyxBotApplicationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class testInitData {

    @Test
    void initAlias() {
        WarframeDataSource.getRivenTrend();
    }
}
