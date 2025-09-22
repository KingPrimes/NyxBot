package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.repo.RivenAnalyseTrendRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Slf4j
public class RivenLookup {
    private static final WeaponsRepository weaponsRepository = SpringUtils.getBean(WeaponsRepository.class);

    private static final RivenAnalyseTrendRepository rivenTrendRepository = SpringUtils.getBean(RivenAnalyseTrendRepository.class);

    // 字符替换映射表：key=需要替换的字符，value=目标字符
    private static final Map<String, String> CHAR_REPLACEMENTS = new LinkedHashMap<>();
    private static final Map<String, String> CHAR_ANALYSE = new LinkedHashMap<>();

    // 静态初始化映射表
    static {
        CHAR_REPLACEMENTS.put("淞", "凇");
    }


    static {
        CHAR_ANALYSE.put("射速", "射速/攻击速度");
        CHAR_ANALYSE.put("攻击速度", "射速/攻击速度");
        CHAR_ANALYSE.put("武器后坐力", "后坐力");
        CHAR_ANALYSE.put("Infested", "对Infested伤害");
        CHAR_ANALYSE.put("lnfested", "对Infested伤害");
        CHAR_ANALYSE.put("Corpus", "对Corpus伤害");
        CHAR_ANALYSE.put("Grinner", "对Grineer伤害");
        CHAR_ANALYSE.put("Grineer", "对Grineer伤害");
        CHAR_ANALYSE.put("滑行", "滑行攻击暴击几率");
        CHAR_ANALYSE.put("暴击几率", "暴击几率");
        CHAR_ANALYSE.put("暴击伤害", "暴击伤害");
        CHAR_ANALYSE.put("秒连击持续时间", "连击持续时间");
        CHAR_ANALYSE.put("连击数", "几率不获得连击数");
        CHAR_ANALYSE.put("冰冻", "冰冻伤害");
        CHAR_ANALYSE.put("毒素", "毒素伤害");
        CHAR_ANALYSE.put("电击", "电击伤害");
        CHAR_ANALYSE.put("火焰", "火焰伤害");
        CHAR_ANALYSE.put("冲击", "冲击伤害");
        CHAR_ANALYSE.put("切割", "切割伤害");
        CHAR_ANALYSE.put("穿刺", "穿刺伤害");
        CHAR_ANALYSE.put("投射物", "投射物飞行速度");
        CHAR_ANALYSE.put("后坐力", "后坐力");
        CHAR_ANALYSE.put("伤害", "伤害/近战伤害");
    }

    public List<Weapons> findByFuzzyName(String name) {
        String fixedName = name;
        if (fixedName != null) {
            for (Entry<String, String> replacement : CHAR_REPLACEMENTS.entrySet()) {
                fixedName = fixedName.replace(replacement.getKey(), replacement.getValue());
            }
        }
        return weaponsRepository.findByNameContaining(fixedName);
    }

    public Optional<RivenAnalyseTrend> findRivenTrendByAnalyseName(String analyseName) {
        // 遍历映射表匹配词条（按优先级顺序）
        for (Map.Entry<String, String> entry : CHAR_ANALYSE.entrySet()) {
            String keyword = entry.getKey();
            String targetName = entry.getValue();

            if (analyseName.contains(keyword)) {
                Optional<RivenAnalyseTrend> trend = rivenTrendRepository.findByName(targetName);
                if (trend.isPresent()) {
                    return trend;
                }
            }
        }

        // 无匹配时使用原始名称查询
        return rivenTrendRepository.findByName(analyseName);
    }
}
