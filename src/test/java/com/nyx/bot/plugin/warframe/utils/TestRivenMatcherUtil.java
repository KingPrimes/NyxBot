package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.utils.RivenMatcherUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestRivenMatcherUtil {

    @Test
    public void testRivenWeaponsName() {
        log.info("是否是武器名称：{}", RivenMatcherUtil.isWeaponsName("信条·集议"));
    }

    @Test
    public void testRivenAttribute() {
        log.info("是否是词条属性：{}", RivenMatcherUtil.whetherItIsDiscrimination("x0.65对Corpus的伤害"));
    }

    @Test
    public void testMarketRiven2() {
        String originalString = "CeramicDagger";
        String modifiedString = originalString.replaceAll("(?<=\\p{Ll})(?=\\p{Lu})", " "); // 在小写字母和大写字母之间添加空格
        log.info("转换后的名称:{}", modifiedString);
    }
}
