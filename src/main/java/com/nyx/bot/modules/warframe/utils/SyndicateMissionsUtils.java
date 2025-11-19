package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.RewardPool;
import io.github.kingprimes.model.enums.SyndicateEnum;
import io.github.kingprimes.model.worldstate.SyndicateMission;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
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
        StateTranslationRepository str = SpringUtils.getBean(StateTranslationRepository.class);
        sms.stream()
                .filter(sm -> Objects.equals(sm.getTag(), se))
                .filter(sm -> sm.getJobs() != null && !sm.getJobs().isEmpty())
                .findFirst()
                .ifPresent(sm -> {
                    sm.setJobs(sm.getJobs().stream()
                            .peek(j ->
                                    str.findByUniqueName(StringUtils.getLastThreeSegments(j.getType() != null ? j.getType() : j.getLocationTag()))
                                            .ifPresent(s -> {
                                                j.setType(s.getName());
                                                j.setDesc(s.getDescription());
                                            })
                            ).peek(j -> SpringUtils.getBean(RewardPoolRepository.class).findById(j.getRewards()).ifPresent(r -> {
                                j.setRewardPool(new RewardPool()
                                        .setRewards(
                                                r.getRewards()
                                                        .stream()
                                                        .map(rd ->
                                                                new RewardPool.Reward()
                                                                        .setItem(rd.getItem())
                                                                        .setItemCount(rd.getItemCount())
                                                                        .setRarity(rd.getRarity())).toList()
                                        )
                                );
                            }))
                            .toList());
                    smr.set(sm);
                });
        return smr.get();
    }

    public static byte[] postSyndicateEntratiImage(SyndicateEnum syndicateEnum) throws DataNotInfoException {
        SyndicateMission syndicateMissions = getSyndicateMissions(WarframeCache.getWarframeStatus().getSyndicateMissions(), syndicateEnum);
        return SpringUtils.getBean(DrawImagePlugin.class).drawSyndicateImage(syndicateMissions);
    }
}
