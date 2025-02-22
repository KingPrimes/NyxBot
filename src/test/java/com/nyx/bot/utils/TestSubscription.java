package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.plugin.warframe.utils.WarframeSubscribeCheck;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestSubscription {

    @Test
    void test() {
        for(int j = 1; j < 15; j++) {
            long bot = ThreadLocalRandom.current().nextLong(11111111,99999999);

            long group = ThreadLocalRandom.current().nextLong(11111111,99999999);
            String uname = StringUtils.getRandomString();
            String gname = StringUtils.getRandomString();
            for (int i = 1; i < 15; i++) {
                long user = ThreadLocalRandom.current().nextLong(11111111,99999999);
                for (int k = 1; k < 15; k++){
                    String s = new WarframeSubscribeCheck().userSubscriptions(
                            bot,
                            user,
                            uname,
                            group,
                            gname,
                            String.valueOf(k));
                    log.info("s:{}", s);
                }

            }
        }

        //String s = new WarframeSubscribeCheck().userSubscriptions(123L, 456L, "testUser", 789L, "testGroup", "订阅9");
        //log.info("s:{}", s);
        //String s1 = new WarframeSubscribeCheck().userCancelSubscribe(166L, 1189L, "取消订阅2");
        //log.info("s1:{}", s1);
    }
}
