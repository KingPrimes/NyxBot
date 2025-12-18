package com.nyx.bot.modules.admin.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.Codes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class UpdateUtils {

    private final WarframeDataSource dataSource;

    public UpdateUtils(WarframeDataSource dataSource) {
        this.dataSource = dataSource;
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
        CompletableFuture.supplyAsync(dataSource::initOrdersItemsData)
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
        CompletableFuture.supplyAsync(dataSource::getRivenWeapons)
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
                .supplyAsync(dataSource::severExportFiles)
                .thenAccept(flag -> {
                    if (!flag) {
                        throw new RuntimeException("翻译数据更新失败！");
                    }
                })
                .thenCompose(ignore -> CompletableFuture.runAsync(dataSource::initStateTranslation));
    }

    private CompletableFuture<Void> initAllTranslations(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "翻译数据已更新完毕！", false);

        CompletableFuture<Void> nodesFuture = runAsyncWithNotification(
                dataSource::initNodes, bot, event, "节点翻译数据已更新完毕！"
        );
        CompletableFuture<Void> weaponsFuture = runAsyncWithNotification(
                dataSource::initWeapons, bot, event, "武器翻译数据已更新完毕！"
        );
        CompletableFuture<Void> rewardPoolFuture = runAsyncWithNotification(
                dataSource::initRewardPool, bot, event, "奖励池翻译数据已更新完毕！"
        );
        CompletableFuture<Void> nightWaveFuture = runAsyncWithNotification(
                dataSource::initNightWave, bot, event, "电波翻译数据已更新完毕！"
        );

        return CompletableFuture.allOf(nodesFuture, weaponsFuture, rewardPoolFuture, nightWaveFuture);
    }

    private CompletableFuture<Void> runAsyncWithNotification(Runnable task, Bot bot, AnyMessageEvent event, String message) {
        return CompletableFuture
                .runAsync(task)
                .thenRun(() -> bot.sendMsg(event, message, false));
    }

    private void updateWarframeSister(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(dataSource::getLichSisterWeapons)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "信条/赤毒武器 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "信条/赤毒武器 更新失败！", false);
                    }
                });
    }
}
