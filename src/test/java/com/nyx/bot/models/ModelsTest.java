package com.nyx.bot.models;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.*;
import com.nyx.bot.utils.http.HttpUtils;
import org.junit.jupiter.api.Test;

public class ModelsTest {

    HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
    WorldState worldState = JSONObject.parseObject(body.getBody(), WorldState.class, JSONReader.Feature.SupportSmartMatch);

    // 夜灵平原
    @Test
    void testCetusCycle() {
        worldState.getSyndicateMissions().stream()
                .filter(s -> s.getTag().equals("CetusSyndicate"))
                .peek(s -> System.out.println(new CetusCycle(s.getExpiry().getEpochSecond()))).count();
    }

    // 魔胎之境
    @Test
    void testCambionCycle() {
        worldState.getSyndicateMissions().stream()
                .filter(s -> s.getTag().equals("CetusSyndicate"))
                .peek(s -> {
                    CambionCycle cambionCycle = new CambionCycle(new CetusCycle(s.getExpiry().getEpochSecond()));
                    System.out.println("CambionCycle:" + cambionCycle);
                }).count();
    }

    // 双衍王境
    @Test
    void testDuviriCycle() {
        DuviriCycle duviriCycle = new DuviriCycle(worldState.getEndlessXpChoices());
        System.out.println("DuviriCycle:" + duviriCycle);
    }

    // 地球循环
    @Test
    void testEarthCycle() {
        EarthCycle earthCycle = new EarthCycle();
        System.out.println("Current State: " + earthCycle);
    }

    // 奥布山谷 轮换
    @Test
    void testVallisCycle(){
        VallisCycle vallisCycle = new VallisCycle();
        System.out.println("VallisCycle:" + vallisCycle);
    }

    @Test
    void testZarimanCycle(){
        worldState.getSyndicateMissions().stream()
                .filter(s -> s.getTag().equals("ZarimanSyndicate"))
                .peek(s -> System.out.println(JSON.toJSONString(new ZarimanCycle(s.getExpiry().getEpochSecond())))).count();
    }


    @Test
    void testWorldState(){

        System.out.println(JSON.toJSONString(worldState));
    }
}
