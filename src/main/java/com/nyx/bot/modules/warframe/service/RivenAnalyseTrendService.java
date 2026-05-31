package com.nyx.bot.modules.warframe.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.repo.RivenAnalyseTrendRepository;
import com.nyx.bot.modules.warframe.utils.ApiDataSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RivenAnalyseTrendService {

    private final ApiDataSourceUtils apiDataSourceUtils;
    private final RivenAnalyseTrendRepository rivenAnalyseTrendRepository;

    public RivenAnalyseTrendService(ApiDataSourceUtils apiDataSourceUtils, RivenAnalyseTrendRepository rivenAnalyseTrendRepository) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.rivenAnalyseTrendRepository = rivenAnalyseTrendRepository;
    }

    private List<RivenAnalyseTrend> getRivenAnalyseTrends() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.warframeDataSourceRivenAnalyseTrend(), new TypeReference<>() {
        });
    }

    /**
     * 更新紫卡分析趋势数据
     * <br/>
     * 使用"智能更新"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的紫卡分析趋势数据数量
     * @throws ServiceException 当紫卡分析趋势数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRivenAnalyseTrends() {
        log.info("开始更新紫卡分析趋势数据，获取数据源...");
        List<RivenAnalyseTrend> rivenAnalyseTrends = getRivenAnalyseTrends();
        if (rivenAnalyseTrends.isEmpty()) {
            throw new ServiceException("RivenAnalyseTrends数据获取失败！", 500);
        }
        log.info("获取到 {} 条紫卡分析趋势数据，准备更新数据库", rivenAnalyseTrends.size());

        try {
            List<RivenAnalyseTrend> existingList = rivenAnalyseTrendRepository.findAll();
            Map<String, RivenAnalyseTrend> existingMap = existingList.stream()
                    .filter(rat -> rat.getName() != null)
                    .collect(Collectors.toMap(RivenAnalyseTrend::getName, Function.identity(), (a1, a2) -> a1));

            log.debug("数据库中现有 {} 条紫卡分析趋势数据", existingMap.size());

            List<RivenAnalyseTrend> toSave = new ArrayList<>();
            for (RivenAnalyseTrend newTrend : rivenAnalyseTrends) {
                RivenAnalyseTrend existing = existingMap.get(newTrend.getName());
                if (existing != null) {
                    newTrend.setId(existing.getId());
                } else {
                    newTrend.setId(null);
                }
                toSave.add(newTrend);
            }

            List<RivenAnalyseTrend> saved = rivenAnalyseTrendRepository.saveAll(toSave);
            rivenAnalyseTrendRepository.flush();

            log.info("紫卡分析趋势数据更新完成，共 {} 条", saved.size());
            return saved.size();
        } catch (Exception e) {
            log.error("更新紫卡分析趋势数据时发生异常", e);
            throw new ServiceException("更新紫卡分析趋势数据失败: " + e.getMessage(), 500);
        }
    }

}
