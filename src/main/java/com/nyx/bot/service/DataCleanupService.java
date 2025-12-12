package com.nyx.bot.service;

import com.nyx.bot.modules.warframe.repo.*;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.repo.exprot.RelicsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DataCleanupService {
    private final StateTranslationRepository str;
    private final NodesRepository nodesRepository;
    private final WeaponsRepository weaponsRepository;
    private final RewardPoolRepository rewardPoolRepository;
    private final AliasRepository aliasRepository;
    private final EphemerasRepository ephemerasRepository;
    private final OrdersItemsRepository ordersItemsRepository;
    private final LichSisterWeaponsRepository lichSisterWeaponsRepository;
    private final RivenItemsRepository rivenItemsRepository;
    private final RelicsRepository relicsRepository;
    private final RivenTionRepository rivenTionRepository;
    private final RivenTionAliasRepository rivenTionAliasRepository;
    private final RivenAnalyseTrendRepository rivenAnalyseTrendRepository;
    private final RewardRepository rewardRepository;
    private final NightWaveRepository nightWaveRepository;

    public DataCleanupService(StateTranslationRepository str, NodesRepository nodesRepository, WeaponsRepository weaponsRepository, RewardPoolRepository rewardPoolRepository, AliasRepository aliasRepository, EphemerasRepository ephemerasRepository, OrdersItemsRepository ordersItemsRepository, LichSisterWeaponsRepository lichSisterWeaponsRepository, RivenItemsRepository rivenItemsRepository, RelicsRepository relicsRepository, RivenTionRepository rivenTionRepository, RivenTionAliasRepository rivenTionAliasRepository, RivenAnalyseTrendRepository rivenAnalyseTrendRepository, RewardRepository rewardRepository, NightWaveRepository nightWaveRepository) {
        this.str = str;
        this.nodesRepository = nodesRepository;
        this.weaponsRepository = weaponsRepository;
        this.rewardPoolRepository = rewardPoolRepository;
        this.aliasRepository = aliasRepository;
        this.ephemerasRepository = ephemerasRepository;
        this.ordersItemsRepository = ordersItemsRepository;
        this.lichSisterWeaponsRepository = lichSisterWeaponsRepository;
        this.rivenItemsRepository = rivenItemsRepository;
        this.relicsRepository = relicsRepository;
        this.rivenTionRepository = rivenTionRepository;
        this.rivenTionAliasRepository = rivenTionAliasRepository;
        this.rivenAnalyseTrendRepository = rivenAnalyseTrendRepository;
        this.rewardRepository = rewardRepository;
        this.nightWaveRepository = nightWaveRepository;
    }

    @Transactional
    public void performAtomicCleanup() {
        log.info("开始原子清理数据库...");

        // 按依赖顺序清理
        List<CleanupTask> cleanupTasks = Arrays.asList(
                new CleanupTask("RivenAnalyseTrend", rivenAnalyseTrendRepository::deleteAll),
                new CleanupTask("RivenTionAlias", rivenTionAliasRepository::deleteAll),
                new CleanupTask("RivenTion", rivenTionRepository::deleteAll),
                new CleanupTask("Relics", relicsRepository::deleteAll),
                new CleanupTask("RivenItems", rivenItemsRepository::deleteAll),
                new CleanupTask("LichSisterWeapons", lichSisterWeaponsRepository::deleteAll),
                new CleanupTask("OrdersItems", ordersItemsRepository::deleteAll),
                new CleanupTask("Ephemeras", ephemerasRepository::deleteAll),
                new CleanupTask("Alias", aliasRepository::deleteAll),
                new CleanupTask("RewardPool", rewardPoolRepository::deleteAll),
                new CleanupTask("Weapons", weaponsRepository::deleteAll),
                new CleanupTask("Nodes", nodesRepository::deleteAll),
                new CleanupTask("StateTranslation", str::deleteAll),
                new CleanupTask("Reward", rewardRepository::deleteAll),
                new CleanupTask("NightWave", nightWaveRepository::deleteAll)
        );

        for (CleanupTask task : cleanupTasks) {
            try {
                log.info("开始清理 {} 表", task.name);
                task.task.run();
            } catch (Exception e) {
                log.error("{} 清理任务失败，继续后续清理", task.name, e);
            }
        }
        log.info("所有表数据清理完成");
    }

    public Long count() {
        return rivenAnalyseTrendRepository.count() + rivenTionAliasRepository.count() + rivenTionRepository.count() + relicsRepository.count() + rivenItemsRepository.count() + lichSisterWeaponsRepository.count() + ordersItemsRepository.count() + ephemerasRepository.count() + aliasRepository.count() + rewardPoolRepository.count() + weaponsRepository.count() + nodesRepository.count() + str.count() + rewardRepository.count() + nightWaveRepository.count();
    }

    private record CleanupTask(String name, Runnable task) {
    }
}
