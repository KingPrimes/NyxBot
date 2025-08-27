package com.nyx.bot.modules.warframe.service;

import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.RelicsRepository;
import com.nyx.bot.modules.warframe.utils.RelicsImportUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class RelicsService {

    @Resource
    RelicsRepository repository;

    @Resource
    AliasRepository ar;

    @Resource
    StateTranslationRepository str;

    public Integer initRelicsData(String filePath) {
        log.info("开始初始化遗物数据");
        return new RelicsImportUtil(str, repository).importRelicsData(filePath);
    }


    public TableDataInfo findAllPageable(Relics relics) {
        if (relics.getName().isEmpty()) relics.setName(null);
        var page = repository.findAllPageable(relics,
                PageRequest.of(
                        relics.getCurrent() - 1,
                        relics.getSize()
                )
        );
        return new TableDataInfo(
                200,
                page.getTotalElements(),
                page.getSize(),
                page.getContent()
        );
    }


    public List<Relics> findAllByRelicNameOrRewardsItemName(String name) {
        // 默认精准查询遗物名称
        var rs = repository.findByName(name);
        if (!rs.isEmpty()) {
            sort(rs);
            return rs;
        }
        log.debug("未查询到遗物名称为 {} 的遗物，进行模糊查询", name);
        // 如果未查询到，进行模糊查询
        rs = repository.findByNameContaining(name);
        if (!rs.isEmpty()) {
            sort(rs);
            return rs;
        }
        log.debug("未查询到包含 {} 的遗物，进行奖励物品的模糊查询", name);
        // 如果遗物名称未查询到，进行奖励物品的查询
        rs = repository.findByRelicRewardsRewardNameContaining(name);
        if (!rs.isEmpty()) {
            sort(rs);
            return rs;
        }
        log.debug("未查询到包含 {} 的遗物，使用别名模糊查询奖励列表", name);
        // 如果奖励物品名称未查询到，使用别名模糊查询奖励列表
        var alias = ar.findAll();
        AtomicReference<String> key = new AtomicReference<>(name);
        alias.forEach(a -> {
            if (key.get().contains(a.getCn())) {
                key.set(key.get().replace(a.getCn(), a.getEn()));
            }
        });
        rs = repository.findByRelicRewardsRewardNameContaining(key.get());
        if (!rs.isEmpty()) {
            sort(rs);
            return rs;
        }
        log.debug("未找到物品:{}", name);
        return new ArrayList<>();
    }

    private void sort(List<Relics> rs) {
        // 排序
        rs.sort(Comparator.comparing(Relics::getName));
    }


}
