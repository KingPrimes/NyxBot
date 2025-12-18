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
        CompletableFuture.supplyAsync(dataSource::severExportFiles).thenAccept(flag -> {
                    if (!flag) {
                        throw new RuntimeException("翻译数据更新失败！");
                    }
                })
                .thenCompose(ignore -> CompletableFuture.runAsync(dataSource::initStateTranslation))
                .thenCompose(ignore -> {
                    bot.sendMsg(event, "翻译数据已更新完毕！", false);
                    // 同时执行所有并行任务
                    CompletableFuture<Void> nodesFuture = CompletableFuture.runAsync(dataSource::initNodes)
                            .thenRun(() -> bot.sendMsg(event, "节点翻译数据已更新完毕！", false));

                    CompletableFuture<Void> weaponsFuture = CompletableFuture.runAsync(dataSource::initWeapons)
                            .thenRun(() -> bot.sendMsg(event, "武器翻译数据已更新完毕！", false));

                    CompletableFuture<Void> rewardPoolFuture = CompletableFuture.runAsync(dataSource::initRewardPool)
                            .thenRun(() -> bot.sendMsg(event, "奖励池翻译数据已更新完毕！", false));

                    CompletableFuture<Void> nightWaveFuture = CompletableFuture.runAsync(dataSource::initNightWave)
                            .thenRun(() -> bot.sendMsg(event, "电波翻译数据已更新完毕！", false));

                    return CompletableFuture.allOf(nodesFuture, weaponsFuture, rewardPoolFuture, nightWaveFuture);
                })
                .thenRun(() -> bot.sendMsg(event, "翻译数据已全部更新完毕！", false))
                .exceptionally(ex -> {
                    log.error("翻译数据更新失败", ex);
                    bot.sendMsg(event, "翻译数据更新失败。请查看具体日志信息。", false);
                    return null;
                });
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
