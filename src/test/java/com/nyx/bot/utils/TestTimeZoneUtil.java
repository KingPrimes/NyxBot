package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestTimeZoneUtil {
    @Test
    void test() {
        System.out.println(TimeZoneUtil.getSystemTimeZone());
        System.out.println(TimeZoneUtil.getAllTimeZones());
        System.out.println(TimeZoneUtil.isValidTimeZone("Asia/Shanghai"));
        System.out.println(TimeZoneUtil.formatTimestamp(System.currentTimeMillis(), "Asia/Shanghai"));
        System.out.println(TimeZoneUtil.getEffectiveTimeZone());
    }
}
