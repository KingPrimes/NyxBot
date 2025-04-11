package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.enums.SyndicateKeyEnum;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestSyndicateMissions {

    @Test
    void test() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_STATUS + "pc");
        GlobalStates globalStates = JSON.parseObject(body.getBody(), GlobalStates.class, JSONReader.Feature.SupportSmartMatch);
        GlobalStates.SyndicateMissions sm = SyndicateMissionsUtils.getSyndicateMissions(globalStates, SyndicateKeyEnum.SOLARIS_UNITED);
        log.info(JSON.toJSONString(sm));
    }
}
