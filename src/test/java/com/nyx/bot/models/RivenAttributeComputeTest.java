package com.nyx.bot.models;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.utils.RivenAttributeCompute;
import com.nyx.bot.utils.ocr.OcrUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class RivenAttributeComputeTest {
    @Test
    void testRivenAttributeCompute() {
        List<String> paths = List.of(
                "D:\\Demos\\NyxBot\\data\\riven\\bingsong.png"
//                "D:\\Demos\\NyxBot\\data\\riven\\chengjiezhe.png",
//                "D:\\Demos\\NyxBot\\data\\riven\\liangziqiege.jpg",
//                "D:\\Demos\\NyxBot\\data\\riven\\zhanren.png"
        );
        for (String path : paths) {
            List<String> strings = OcrUtil.ocrPath(path);
            log.debug("识别文字：{}\n", strings);
            List<List<String>> list = new ArrayList<>();
            list.add(strings);
            List<RivenAnalyseTrendCompute> riven = RivenAttributeCompute.getRiven(list);
            log.debug("识别到的武器：{}", riven);
            List<List<RivenAnalyseTrendModel>> lists = RivenAttributeCompute.setAttributeNumber(riven);
            log.debug("计算后的武器：{}", lists);
        }
    }
}
