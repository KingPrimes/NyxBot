package com.nyx.bot.data;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.modules.warframe.service.*;
import com.nyx.bot.task.TaskWarframeStatus;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.model.WorldState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@SuppressWarnings("all")
public class WarframeDataSource {

    private final ObjectMapper objectMapper;
    private final AliasService aliasService;
    private final EphemerasService ephemerasService;
    private final OrdersItemsService ordersItemsService;
    private final LichSisterWeaponsService lichSisterWeaponsService;
    private final RivenItemsService rivenItemsService;
    private final RelicsService relicsService;
    private final RivenTionService rivenTionService;
    private final RivenTionAliasService rivenTionAliasService;
    private final RivenAnalyseTrendService rivenAnalyseTrendService;
    private final TaskWarframeStatus taskWarframeStatus;
    /**
     * 以下为数据初始化专用 Service
     */
    private final RewardPoolService rewardPoolService;
    private final WeaponService weaponService;
    private final WarframeService warframeService;
    private final NightWaveService nightWaveService;
    private final NodeService nodeService;
    private final StateTranslationService stateTranslationService;

    public WarframeDataSource(ObjectMapper objectMapper,
                              AliasService aliasService,
                              EphemerasService ephemerasService,
                              OrdersItemsService ordersItemsService,
                              LichSisterWeaponsService lichSisterWeaponsService,
                              RivenItemsService rivenItemsService,
                              RelicsService relicsService,
                              RivenTionService rivenTionService,
                              RivenTionAliasService rivenTionAliasService,
                              RivenAnalyseTrendService rivenAnalyseTrendService,
                              TaskWarframeStatus taskWarframeStatus,
                              RewardPoolService rewardPoolService,
                              WeaponService weaponService,
                              WarframeService warframeService,
                              NightWaveService nightWaveService,
                              NodeService nodeService,
                              StateTranslationService stateTranslationService) {
        this.objectMapper = objectMapper;
        this.aliasService = aliasService;
        this.ephemerasService = ephemerasService;
        this.ordersItemsService = ordersItemsService;
        this.lichSisterWeaponsService = lichSisterWeaponsService;
        this.rivenItemsService = rivenItemsService;
        this.relicsService = relicsService;
        this.rivenTionService = rivenTionService;
        this.rivenTionAliasService = rivenTionAliasService;
        this.rivenAnalyseTrendService = rivenAnalyseTrendService;
        this.taskWarframeStatus = taskWarframeStatus;
        this.rewardPoolService = rewardPoolService;
        this.weaponService = weaponService;
        this.warframeService = warframeService;
        this.nightWaveService = nightWaveService;
        this.nodeService = nodeService;
        this.stateTranslationService = stateTranslationService;
    }

    /**
     * 分阶段降级初始化：
     * Phase 0 下载失败 → 使用本地缓存；Phase 1/2 子任务失败 → 事务回滚，不影响其他数据。
     * 不再全量清库和强制退出。
     */
    public void init() {
        log.info("开始初始化数据！");

        Executor executor = SpringUtils.getBean("initDataExecutor");

        // Phase 1c: 无 Phase 0 依赖的独立 HTTP 任务，与 Phase 0 并行启动
        // 直接调用 Service 代理，@Transactional 生效
        CompletableFuture<Void> independentHttpTasks = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> ordersItemsService.initOrdersItemsData(), executor),
                CompletableFuture.runAsync(() -> rewardPoolService.initRewardPool(), executor),
                CompletableFuture.runAsync(this::initWarframeStatus, executor),
                CompletableFuture.runAsync(() -> ephemerasService.initEphemerasData(), executor),
                CompletableFuture.runAsync(() -> lichSisterWeaponsService.initLichSisterWeaponsData(), executor),
                CompletableFuture.runAsync(() -> rivenItemsService.initRivenItemsData(), executor)
        );

        CompletableFuture
                // Phase 0: 下载导出文件（失败时检查本地缓存降级）
                .supplyAsync(() -> {
                    try {
                        return ExportFilePath.severExportFiles();
                    } catch (Exception e) {
                        log.warn("下载导出数据失败: {}", e.getMessage());
                        return false;
                    }
                }, executor)
                .thenCompose(downloadOk -> {
                    if (!downloadOk && !ExportFilePath.localCacheExists()) {
                        log.error("本地无导出数据缓存且下载失败，无法继续初始化");
                        throw new RuntimeException("初始化失败: 无可用数据源");
                    }
                    if (!downloadOk) {
                        log.warn("将使用本地缓存的导出数据进行初始化");
                    }
                    // Phase 1: 翻译数据（失败可跳过，不影响其他模块）
                    return CompletableFuture
                            .runAsync(() -> stateTranslationService.initData(), executor)
                            .exceptionally(ex -> {
                                log.warn("初始化翻译数据失败，翻译功能不可用: {}", ex.getMessage());
                                return null;
                            });
                })
                // Phase 2: 所有依赖 Phase 0/Phase 1 的任务并行执行
                // 直接调用 Service 代理，路径由 Service 内部通过 ExportFilePath 解析
                // 使用 thenCompose 链式组合，避免 .join() 阻塞虚拟线程并持有 Semaphore 许可
                .thenCompose(v -> CompletableFuture.allOf(
                        CompletableFuture.runAsync(() -> aliasService.updateAlias(), executor),
                        CompletableFuture.runAsync(() -> rivenTionService.updateRivenTion(), executor),
                        CompletableFuture.runAsync(() -> rivenTionAliasService.updateRivenTionAlias(), executor),
                        CompletableFuture.runAsync(() -> rivenAnalyseTrendService.updateRivenAnalyseTrends(), executor),
                        CompletableFuture.runAsync(() -> nodeService.initData(), executor),
                        CompletableFuture.runAsync(() -> weaponService.initFromExport(), executor),
                        CompletableFuture.runAsync(() -> nightWaveService.initFromExport(), executor),
                        CompletableFuture.runAsync(() -> warframeService.initFromExport(), executor),
                        CompletableFuture.runAsync(() -> relicsService.initRelicsData(), executor),
                        independentHttpTasks
                ))
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.error("数据初始化过程中出现错误，部分数据可能不可用: {}", ex.getMessage());
                    }
                    // 无论是否有错误，启动定时任务
                    taskWarframeStatus.startSchedule();
                    log.info("数据初始化完成！");
                });
    }

    @SneakyThrows
    public void initWarframeStatus() {
        String a = FileUtils.readFileToString("./data/arbitration");
        String str = FileUtils.readFileToString("./data/status");
        if (!str.isEmpty()) {
            WorldState worldState = objectMapper.readValue(str, WorldState.class);
            WarframeCache.setWarframeStatus(worldState, str, 300);
        }
        if (!a.isEmpty()) {
            List arbitration = objectMapper.readValue(Base64.getDecoder().decode(a), List.class);
            ArbitrationCache.setArbitration(arbitration);
        } else {
            ArbitrationCache.reloadArbitration();
        }
    }

}
