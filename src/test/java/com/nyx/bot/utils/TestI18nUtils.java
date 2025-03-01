package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestI18nUtils {

    @Test
    void testDefaultLocale() {
        String message = I18nUtils.message("warframe.night.cetusCycle");
        Assertions.assertEquals("夜灵平野即将白昼！\n 距离白昼还剩 :", message);
    }
}
