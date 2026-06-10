package com.nyx.bot.modules.admin.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class UpdateUtils {

    private final OrdersItemsService ordersItemsService;
    private final RivenItemsService rivenItemsService;
    private final LichSisterWeaponsService lichSisterWeaponsService;
    private final NodeService nodeService;
    private final WeaponService weaponService;
    private final RewardPoolService rewardPoolService;
    private final NightWaveService nightWaveService;
    private final StateTranslationService stateTranslationService;
    /**
     * 虚拟线程执行器（受 Semaphore 限流保护）
     */
    private final ExecutorService executor;

    public UpdateUtils(OrdersItemsService ordersItemsService,
                       RivenItemsService rivenItemsService,
                       LichSisterWeaponsService lichSisterWeaponsService,
                       NodeService nodeService,
                       WeaponService weaponService,
                       RewardPoolService rewardPoolService,
                       NightWaveService nightWaveService,
                       StateTranslationService stateTranslationService,
                       ExecutorService myAsync) {
        this.ordersItemsService = ordersItemsService;
        this.rivenItemsService = rivenItemsService;
        this.lichSisterWeaponsService = lichSisterWeaponsService;
        this.nodeService = nodeService;
        this.weaponService = weaponService;
        this.rewardPoolService = rewardPoolService;
        this.nightWaveService = nightWaveService;
        this.stateTranslationService = stateTranslationService;
        this.executor = myAsync;
    }

    /**
     * 执行更新内容
     */
    public void updatePlugin(Bot bot, AnyMessageEvent event, Codes codes) {
        switch (codes) {
            case UPDATE_WARFRAME_RES_MARKET_ITEMS: {
                updateWarframeResMarketItems(bot, event);
                break;
            }
            case UPDATE_WARFRAME_RES_MARKET_RIVEN: {
                updateWarframeResMarketRiven(bot, event);
                break;
            }
            case UPDATE_WARFRAME_SISTER: {
                updateWarframeSister(bot, event);
                break;
            }
            case UPDATE_WARFRAME_TAR: {
                updateWarframeTar(bot, event);
                break;
            }
            default:
        }
    }

    private void updateWarframeResMarketItems(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(ordersItemsService::initOrdersItemsData, executor)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "Market 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "Market 更新失败！", false);
                    }
                });
    }

    private void updateWarframeResMarketRiven(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(rivenItemsService::initRivenItemsData, executor)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "WM紫卡 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "WM紫卡 更新失败！", false);
                    }
                });
    }

    private void updateWarframeTar(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        exportFilesAndInitState()
                .thenCompose(ignore -> initAllTranslations(bot, event))
                .thenRun(() -> bot.sendMsg(event, "翻译数据已全部更新完毕！", false))
                .exceptionally(ex -> {
                    log.error("翻译数据更新失败", ex);
                    bot.sendMsg(event, "翻译数据更新失败。请查看具体日志信息。", false);
                    return null;
                });
    }

    private CompletableFuture<Void> exportFilesAndInitState() {
        return CompletableFuture
                .supplyAsync(ExportFilePath::severExportFiles, executor)
                .thenAccept(flag -> {
                    if (!flag) {
                        throw new RuntimeException("翻译数据更新失败！");
                    }
                })
                .thenCompose(ignore -> CompletableFuture.runAsync(stateTranslationService::initData, executor));
    }

    private CompletableFuture<Void> initAllTranslations(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "翻译数据已更新完毕！", false);

        CompletableFuture<Void> nodesFuture = runAsyncWithNotification(
                nodeService::initData, bot, event, "节点翻译数据已更新完毕！"
        );
        CompletableFuture<Void> weaponsFuture = runAsyncWithNotification(
                weaponService::initFromExport, bot, event, "武器翻译数据已更新完毕！"
        );
        CompletableFuture<Void> rewardPoolFuture = runAsyncWithNotification(
                rewardPoolService::initRewardPool, bot, event, "奖励池翻译数据已更新完毕！"
        );
        CompletableFuture<Void> nightWaveFuture = runAsyncWithNotification(
                nightWaveService::initFromExport, bot, event, "电波翻译数据已更新完毕！"
        );

        return CompletableFuture.allOf(nodesFuture, weaponsFuture, rewardPoolFuture, nightWaveFuture);
    }

    private CompletableFuture<Void> runAsyncWithNotification(Runnable task, Bot bot, AnyMessageEvent event, String message) {
        return CompletableFuture
                .runAsync(task, executor)
                .thenRun(() -> bot.sendMsg(event, message, false));
    }

    private void updateWarframeSister(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(lichSisterWeaponsService::initLichSisterWeaponsData, executor)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "信条/赤毒武器 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "信条/赤毒武器 更新失败！", false);
                    }
                });
    }
}
