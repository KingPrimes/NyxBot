package com.nyx.bot.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

//@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestApi {


    @Test
    void testGetWorldState() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            WorldState worldState = JSONObject.parseObject(body.getBody(), WorldState.class);
            log.info(JSON.toJSONString(worldState));
        }
    }

    @Test
    void testUnzip() throws IOException {
        Boolean zh = HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted("zh"), "./data/lzma/index_zh.txt.lzma");
        if (zh) {
            ZipUtils.unLzma("./data/lzma/index_zh.txt.lzma", "./data/lzma/index_zh.txt");
        }
    }


}
