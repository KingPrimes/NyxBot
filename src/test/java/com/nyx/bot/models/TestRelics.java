package com.nyx.bot.models;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.service.RelicsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestRelics {

    @Resource
    RelicsService relicsService;

    @Test
    void testRelics() {
        List<Relics> relics = relicsService.findAllByRelicNameOrRewardsItemName("A1");
        log.info(JSON.toJSONString(relics.stream().limit(9)));
    }
}
