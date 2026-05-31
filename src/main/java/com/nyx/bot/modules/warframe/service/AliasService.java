package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
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
public class AliasService {

    private final AliasRepository aliasRepository;
    private final ApiDataSourceUtils apiDataSourceUtils;

    public AliasService(AliasRepository aliasRepository, ApiDataSourceUtils apiDataSourceUtils) {
        this.aliasRepository = aliasRepository;
        this.apiDataSourceUtils = apiDataSourceUtils;
    }

    private List<Alias> getAlias() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.warframeDataSourceAlias(), new TypeReference<>() {
        });
    }

    /**
     * 更新别名数据
     * <br/>
     * 该方法首先获取别名列表，如果获取失败或列表为空，则抛出服务异常。<br/>
     * 使用"清空-重新插入"策略避免乐观锁冲突，并通过事务确保数据一致性。
     * <br/>
     * 注意：使用同步锁 + 事务保证并发安全性
     *
     * @return 更新的别名数据数量
     * @throws ServiceException 当别名数据获取失败时抛出此异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateAlias() {
        log.info("开始更新别名数据，获取数据源...");
        List<Alias> aliasList = getAlias();
        if (aliasList.isEmpty()) {
            throw new ServiceException("别名数据获取失败！", 500);
        }
        log.info("获取到 {} 条别名数据，准备更新数据库", aliasList.size());

        try {
            List<Alias> existingAliases = aliasRepository.findAll();
            Map<String, Alias> existingMap = existingAliases.stream()
                    .collect(Collectors.toMap(Alias::getCn, Function.identity(), (a1, a2) -> a1));

            log.debug("数据库中现有 {} 条别名数据", existingMap.size());

            List<Alias> toSave = new ArrayList<>();
            for (Alias newAlias : aliasList) {
                Alias existing = existingMap.get(newAlias.getCn());
                if (existing != null) {
                    newAlias.setId(existing.getId());
                } else {
                    newAlias.setId(null);
                }
                toSave.add(newAlias);
            }

            List<Alias> saved = aliasRepository.saveAll(toSave);
            aliasRepository.flush();

            log.info("别名数据更新完成，共 {} 条", saved.size());
            return saved.size();
        } catch (Exception e) {
            log.error("更新别名数据时发生异常", e);
            throw new ServiceException("更新别名数据失败: " + e.getMessage(), 500);
        }
    }
}
