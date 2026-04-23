package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.utils.riven_calculation.RivenAttributeCompute;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestRivenAttributeCompute {
    String json = """
            {
              "weaponsName": "执法者",
              "rivenName": "Para-visitis",
              "attributes": [
                {
                  "name": "近战伤害",
                  "attribute": 201.8,
                  "attributeName": "+201.8%近战伤害",
                  "nag": true,
                  "lowAttribute": 0.0,
                  "highAttribute": 0.0
                },
                {
                  "name": "初始连击",
                  "attribute": 31.7,
                  "attributeName": "+31.7初始连击",
                  "nag": true,
                  "lowAttribute": 0.0,
                  "highAttribute": 0.0
                },
                {
                  "name": "暴击伤害",
                  "attribute": 103.1,
                  "attributeName": "+103.1%暴击伤害",
                  "nag": true,
                  "lowAttribute": 0.0,
                  "highAttribute": 0.0
                },
                {
                  "name": "攻击速度",
                  "attribute": -50.4,
                  "attributeName": "-50.4%攻击速度",
                  "nag": true,
                  "lowAttribute": 0.0,
                  "highAttribute": 0.0
                }
              ]
            }
            """;
    String fuf= """
            {
               "weaponsName": "执法者",
               "rivenName": "critatis",
               "attributes": [
                 {
                   "name": "暴击伤害",
                   "attribute": 103.0,
                   "attributeName": "+103%暴击伤害",
                   "nag": true,
                   "lowAttribute": 0.0,
                   "highAttribute": 0.0
                 },
                 {
                   "name": "暴击几率（重击时 x2）",
                   "attribute": 218.2,
                   "attributeName": "+218.2%暴击几率(重击时 x2)",
                   "nag": true,
                   "lowAttribute": 0.0,
                   "highAttribute": 0.0
                 },
                 {
                   "name": "近战伤害",
                   "attribute": 205.5,
                   "attributeName": "+205.5%近战伤害",
                   "nag": true,
                   "lowAttribute": 0.0,
                   "highAttribute": 0.0
                 },
                 {
                   "name": "攻击范围",
                   "attribute": -2.0,
                   "attributeName": "-2攻击范围",
                   "nag": true,
                   "lowAttribute": 0.0,
                   "highAttribute": 0.0
                 }
               ]
             }
            """;

    List<String> images = List.of("18-", "执法者 Visi-critatis"," +103% 暴击伤害", "+218.2% 暴击几率 (重击", "时×2)", "+205.5% 近战伤害", "-2 攻击范围", "段位8", "5 82");
    List<String> image2 = List.of("委", "181", "豪猪 Acri-satimag", "+108.8% 暴击伤害", "-93.1% 武器后坐力", "+132.9% 多重射击", "x0.63 对 Corpus 的伤害", "段位8");
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testAttributeCompute() throws JsonProcessingException {
        RivenAnalyseTrendCompute riven = objectMapper.readValue(fuf, RivenAnalyseTrendCompute.class);
        log.info("测试读取到的json数据是否正确：{}", riven);
        List<RivenAnalyseTrendModel> rivenAnalyseTrendModels = RivenAttributeCompute.setAttributeNumber(riven);
        log.info("测试计算结果：{}", objectMapper.writeValueAsString(rivenAnalyseTrendModels));
    }

    @Test
    public void testGetRiven(){
        RivenAnalyseTrendCompute riven = RivenAttributeCompute.getRiven(images);
        log.debug(riven.toString());
    }
}
