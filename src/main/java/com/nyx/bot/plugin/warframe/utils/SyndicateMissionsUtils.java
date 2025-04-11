package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.enums.SyndicateKeyEnum;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.SpringUtils;

import java.util.Comparator;

public class SyndicateMissionsUtils {

    /**
     * 获取集团任务列表
     *
     * @param gs           全局状态
     * @param syndicateKey 集团派系
     * @return 格式化之后的集团任务
     */
    public static GlobalStates.SyndicateMissions getSyndicateMissions(GlobalStates gs, SyndicateKeyEnum syndicateKey) {
        TranslationService tr = SpringUtils.getBean(TranslationService.class);
        return gs.getSyndicateMissions().stream()
                // 过滤出指定集团派系
                .filter(s -> s.getSyndicateKey().equals(syndicateKey.getKey()))
                // 获取指定集团派系的任务
                .findFirst()
                // 设置任务
                .map(sm -> {
                    // 设置任务奖励
                    sm.setJobs(sm.getJobs().stream()
                            .peek(j -> {
                                // 翻译任务奖励到中文
                                j.setRewardPool(
                                        j.getRewardPool().stream().map(tr::enToZh).toList()
                                );
                                // 翻译任务类型到中文
                                j.setType(tr.enToZh(j.getType()));
                            })

                            // 排序
                            .sorted(Comparator.comparing(o -> o.getEnemyLevels().getFirst()))
                            .toList()
                    );
                    switch (syndicateKey) {
                        case OSTRONS -> sm.setSyndicate("希图斯");
                        case ENTRATI -> sm.setSyndicate("英择谛");
                        case SOLARIS_UNITED -> sm.setSyndicate("索拉里斯");
                        case ARBITERS_OF_HEXIS -> sm.setSyndicate("均衡仲裁者");
                        case CEPHALON_SUDA -> sm.setSyndicate("中枢苏达");
                        case NEW_LOKA -> sm.setSyndicate("新世间");
                        case PERRIN_SEQUENCE -> sm.setSyndicate("佩兰数列");
                        case RED_VEIL -> sm.setSyndicate("血色面纱");
                        case STEEL_MERIDIAN -> sm.setSyndicate("钢铁防线");
                        default -> {
                        }
                    }
                    return sm;
                }).orElse(new GlobalStates.SyndicateMissions());
    }
}
