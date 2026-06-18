package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.utils.ApiDataSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 奖励池数据初始化服务
 *
 * @author KingPrimes
 */
@Slf4j
@Service
public class RewardPoolService {

    private final ApiDataSourceUtils apiDataSourceUtils;
    private final RewardPoolRepository rewardPoolRepository;

    public RewardPoolService(ApiDataSourceUtils apiDataSourceUtils, RewardPoolRepository rewardPoolRepository) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.rewardPoolRepository = rewardPoolRepository;
    }

    /**
     * 从 CDN 数据源初始化奖励池数据
     *
     * @return 保存的数据条数
     */
    @Transactional
    public int initRewardPool() {
        log.info("开始初始化 自定义 RewardPool reward_pool.json 数据！");
        List<RewardPool> javaList = apiDataSourceUtils.getDataFromSources(
                ApiUrl.warframeDataSourceRewardPool(),
                new TypeReference<>() {
                });
        int size = rewardPoolRepository.saveAll(javaList).size();
        log.info("初始化 自定义 RewardPool reward_pool.json 数据完成，共{}条", size);
        return size;
    }
}
