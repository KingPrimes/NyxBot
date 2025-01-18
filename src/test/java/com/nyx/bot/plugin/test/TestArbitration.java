package com.nyx.bot.plugin.test;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import static com.nyx.bot.core.ApiUrl.WARFRAME_ARBITRATION;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestArbitration {

    @Test
    void testArbitration() {
        List<ArbitrationPre> arbitrationPres = JSON.parseArray(HttpUtils.sendGet(WARFRAME_ARBITRATION).getBody(), ArbitrationPre.class, JSONReader.Feature.SupportSmartMatch);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
        Date date = new Date(now.toEpochSecond(ZoneOffset.ofHours(8)) * 1000L);
        log.debug("now:{}", date);
        arbitrationPres.forEach(arbitrationPre -> {
            long dateHour = DateUtils.getDateSecond(arbitrationPre.getActivation(), date);
            String dateFormat = DateUtils.format(date, DateUtils.YYYY);
            String activationFormat = DateUtils.format(arbitrationPre.getActivation(), DateUtils.YYYY);
            log.debug("dateHour:{} - {} = {}", dateFormat, activationFormat, dateHour);
        });
    }
}
