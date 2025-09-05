package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.res.enums.SyndicateEnum;
import com.nyx.bot.modules.warframe.res.worldstate.SyndicateMission;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class SyndicateMissionsUtils {

    /**
     * 获取集团任务列表
     *
     * @param sms 集团列表
     * @param se  集团派系
     * @return 格式化之后的集团任务
     */
    public static SyndicateMission getSyndicateMissions(List<SyndicateMission> sms, SyndicateEnum se) {
        AtomicReference<SyndicateMission> smr = new AtomicReference<>(new SyndicateMission());
        sms.stream().filter(sm -> sm.getTag().equals(se))
                .filter(sm -> sm.getJobs() != null && !sm.getJobs().isEmpty())
                .findFirst()
                .ifPresent(sm -> {
                    sm.setJobs(sm.getJobs().stream()
                            .peek(j ->
                                    SpringUtils.getBean(StateTranslationRepository.class)
                                            .findByUniqueName(StringUtils.getLastThreeSegments(j.getType()!= null?j.getType():j.getLocationTag()))
                                            .ifPresent(s -> {
                                                j.setType(s.getName());
                                                j.setDesc(s.getDescription());
                                            })
                            ).peek(j -> SpringUtils.getBean(RewardPoolRepository.class).findById(j.getRewards()).ifPresent(j::setRewardPool))
                            .toList());
                    smr.set(sm);
                });
        return smr.get();
    }

    public static byte[] postSyndicateEntratiImage(SyndicateEnum syndicateEnum) throws DataNotInfoException, HtmlToImageException {
        SyndicateMission syndicateMissions = getSyndicateMissions(WarframeCache.getWarframeStatus().getSyndicateMissions(), syndicateEnum);
        return HtmlToImage.generateImage("html/syndicateMissions", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("sm", syndicateMissions);
            return modelMap;
        }).toByteArray();
    }
}
