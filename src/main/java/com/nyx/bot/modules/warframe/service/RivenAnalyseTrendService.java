package com.nyx.bot.modules.warframe.service;

import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.repo.RivenAnalyseTrendRepository;
import com.nyx.bot.modules.warframe.utils.riven_calculation.RivenTrendGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 紫卡分析趋势服务
 * <p>
 * 从 DE 官方导出数据 {@code ExportUpgrades.json} 中自动计算趋势值并入库，
 * 不再依赖 CDN 老旧数据。
 * </p>
 */
@Slf4j
@Service
public class RivenAnalyseTrendService {

    private final RivenTrendGenerator rivenTrendGenerator;
    private final RivenAnalyseTrendRepository rivenAnalyseTrendRepository;

    public RivenAnalyseTrendService(RivenTrendGenerator rivenTrendGenerator,
                                    RivenAnalyseTrendRepository rivenAnalyseTrendRepository) {
        this.rivenTrendGenerator = rivenTrendGenerator;
        this.rivenAnalyseTrendRepository = rivenAnalyseTrendRepository;
    }

    /**
     * 从 DE 导出文件计算并更新紫卡分析趋势数据
     * <p>
     * 使用"智能更新"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * </p>
     *
     * @return 更新的紫卡分析趋势数据数量
     * @throws ServiceException 当紫卡分析趋势数据生成失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRivenAnalyseTrends() {
        log.info("开始从 DE 导出数据生成紫卡分析趋势...");
        List<RivenAnalyseTrend> generated = rivenTrendGenerator.generate();
        if (generated.isEmpty()) {
            throw new ServiceException("从 DE 导出数据生成紫卡趋势失败！", 500);
        }
        log.info("生成 {} 条紫卡分析趋势数据，准备更新数据库", generated.size());

        try {
            List<RivenAnalyseTrend> existingList = rivenAnalyseTrendRepository.findAll();
            Map<String, RivenAnalyseTrend> existingMap = existingList.stream()
                    .filter(rat -> rat.getName() != null)
                    .collect(Collectors.toMap(RivenAnalyseTrend::getName, Function.identity(), (a1, a2) -> a1));

            log.debug("数据库中现有 {} 条紫卡分析趋势数据", existingMap.size());

            List<RivenAnalyseTrend> toSave = new ArrayList<>();
            for (RivenAnalyseTrend newTrend : generated) {
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
