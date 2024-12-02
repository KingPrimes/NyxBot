package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.ApiUrl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestMission {

    @Test
    public void testArbitration() {
        log.info(JSON.toJSONString(ApiUrl.arbitrationPre()));
    }
}
