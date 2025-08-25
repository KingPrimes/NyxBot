package com.nyx.bot.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.utils.http.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestHttpUtils {

    @Test
    public void testGet() {
        for (int i = 0; i < 10; i++) {
            HttpUtils.sendGet(ApiUrl.WARFRAME_STATUS + "/pc");
        }
    }

}
