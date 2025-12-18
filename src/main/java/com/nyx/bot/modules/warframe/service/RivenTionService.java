package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.RivenTion;
import com.nyx.bot.modules.warframe.repo.RivenTionRepository;
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
public class RivenTionService {


    /**
     * 同步锁，用于防止并发更新紫卡词条数据时的乐观锁冲突
     */
    private static final Object RIVEN_TION_UPDATE_LOCK = new Object();
    private final ApiDataSourceUtils apiDataSourceUtils;
    private final RivenTionRepository rivenTionRepository;

    public RivenTionService(ApiDataSourceUtils apiDataSourceUtils, RivenTionRepository rivenTionRepository) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.rivenTionRepository = rivenTionRepository;
    }

    private List<RivenTion> getRivenTions() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.WARFRAME_DATA_SOURCE_MARKET_RIVEN_TION, new TypeReference<>() {
        });
    }

    /**
     * 更新紫卡词条数据
     * <br/>
     * 使用"智能更新"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的紫卡词条数据数量
     * @throws ServiceException 当紫卡词条数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRivenTion() {
        synchronized (RIVEN_TION_UPDATE_LOCK) {
            log.debug("开始更新紫卡词条数据，获取数据源...");
            List<RivenTion> rivenTionList = getRivenTions();
            if (rivenTionList.isEmpty()) {
                throw new ServiceException("RivenTion数据获取失败！", 500);
            }
            log.debug("获取到 {} 条紫卡词条数据，准备更新数据库", rivenTionList.size());

            try {
                // 策略：查询现有数据，使用唯一约束字段 url_name 进行映射
                List<RivenTion> existingList = rivenTionRepository.findAll();
                Map<String, RivenTion> existingMap = existingList.stream()
                        .filter(rt -> rt.getUrlName() != null)
                        .collect(Collectors.toMap(RivenTion::getUrlName, Function.identity(), (a1, a2) -> a1));

                log.debug("数据库中现有 {} 条紫卡词条数据", existingMap.size());

                // 处理新数据：为已存在的记录复用ID
                List<RivenTion> toSave = new ArrayList<>();
                for (RivenTion newRivenTion : rivenTionList) {
                    RivenTion existing = existingMap.get(newRivenTion.getUrlName());
                    if (existing != null) {
                        // 存在相同的 url_name，复用ID
                        newRivenTion.setIds(existing.getIds());
                    } else {
                        // 新数据，确保ID为null以便自动生成
                        newRivenTion.setIds(null);
                    }
                    toSave.add(newRivenTion);
                }

                // 批量保存
                List<RivenTion> saved = rivenTionRepository.saveAll(toSave);
                rivenTionRepository.flush();

                log.debug("紫卡词条数据更新完成，共 {} 条", saved.size());
                return saved.size();
            } catch (Exception e) {
                log.error("更新紫卡词条数据时发生异常", e);
                throw new ServiceException("更新紫卡词条数据失败: " + e.getMessage(), 500);
            }
        }
    }

}
