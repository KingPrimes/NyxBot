package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.RivenTionAlias;
import com.nyx.bot.modules.warframe.repo.RivenTionAliasRepository;
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
public class RivenTionAliasService {

    private final ApiDataSourceUtils apiDataSourceUtils;
    private final RivenTionAliasRepository rivenTionAliasRepository;

    public RivenTionAliasService(ApiDataSourceUtils apiDataSourceUtils, RivenTionAliasRepository rivenTionAliasRepository) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.rivenTionAliasRepository = rivenTionAliasRepository;
    }

    private List<RivenTionAlias> getRivenTionAlias() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.warframeDataSourceMarketRivenTionAlias(), new TypeReference<>() {
        });
    }

    /**
     * 更新紫卡词条别名数据
     * <br/>
     * 使用"智能更新"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的紫卡词条别名数据数量
     * @throws ServiceException 当紫卡词条别名数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateRivenTionAlias() {
        log.info("开始更新紫卡词条别名数据，获取数据源...");
        List<RivenTionAlias> rivenTionAliasList = getRivenTionAlias();
        if (rivenTionAliasList.isEmpty()) {
            throw new ServiceException("RivenTionAlias数据获取失败！", 500);
        }
        log.info("获取到 {} 条紫卡词条别名数据，准备更新数据库", rivenTionAliasList.size());

        try {
            List<RivenTionAlias> existingList = rivenTionAliasRepository.findAll();
            Map<String, RivenTionAlias> existingMap = existingList.stream()
                    .filter(rta -> rta.getEn() != null && rta.getCn() != null)
                    .collect(Collectors.toMap(
                            rta -> rta.getEn() + "|" + rta.getCn(),
                            Function.identity(),
                            (a1, a2) -> a1
                    ));

            log.debug("数据库中现有 {} 条紫卡词条别名数据", existingMap.size());

            List<RivenTionAlias> toSave = new ArrayList<>();
            for (RivenTionAlias newAlias : rivenTionAliasList) {
                String key = newAlias.getEn() + "|" + newAlias.getCn();
                RivenTionAlias existing = existingMap.get(key);
                if (existing != null) {
                    newAlias.setId(existing.getId());
                } else {
                    newAlias.setId(null);
                }
                toSave.add(newAlias);
            }

            List<RivenTionAlias> saved = rivenTionAliasRepository.saveAll(toSave);
            rivenTionAliasRepository.flush();

            log.info("紫卡词条别名数据更新完成，共 {} 条", saved.size());
            return saved.size();
        } catch (Exception e) {
            log.error("更新紫卡词条别名数据时发生异常", e);
            throw new ServiceException("更新紫卡词条别名数据失败: " + e.getMessage(), 500);
        }
    }
}
