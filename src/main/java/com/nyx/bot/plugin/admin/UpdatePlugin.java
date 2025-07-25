package com.nyx.bot.plugin.admin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.utils.SystemInfoUtils;
import com.nyx.bot.utils.UpdateJarUtils;
import com.nyx.bot.utils.gitutils.GitHubUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class UpdatePlugin {

    /**
     * 执行更新内容
     */
    public static void updatePlugin(Bot bot, AnyMessageEvent event, Codes codes) {
        switch (codes) {
            case UPDATE_HTML: {
                updateHtml(bot, event);
                break;
            }
            case UPDATE_WARFRAME_RES_MARKET_ITEMS: {
                updateWarframeResMarketItems(bot, event);
                break;
            }
            case UPDATE_WARFRAME_RES_MARKET_RIVEN: {
                updateWarframeResMarketRiven(bot, event);
                break;
            }
            case UPDATE_WARFRAME_RES_RM: {
                updateWarframeResRm(bot, event);
                break;
            }
            case UPDATE_WARFRAME_RIVEN_CHANGES: {
                updateWarframeRivenChanges(bot, event);
                break;
            }
            case UPDATE_WARFRAME_SISTER: {
                updateWarframeSister(bot, event);
                break;
            }
//            case UPDATE_WARFRAME_TAR: {
//                updateWarframeTar(bot, event);
//                break;
//            }
            case UPDATE_JAR: {
                updateJar(bot, event);
                break;
            }
            default:
        }
    }


    private static void updateHtml(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
            if (flag) {
                bot.sendMsg(event, "HTML模板，更新成功！", false);
            } else {
                bot.sendMsg(event, "HTML模板，更新失败！", false);
            }
        });
    }

    private static void updateWarframeResMarketItems(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(WarframeDataSource::getMarket)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "Market 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "Market 更新失败！", false);
                    }
                });
    }

    private static void updateWarframeResMarketRiven(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(WarframeDataSource::getRivenWeapons)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "WM紫卡 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "WM紫卡 更新失败！", false);
                    }
                });
    }

    private static void updateWarframeResRm(Bot bot, AnyMessageEvent event) {
        // TODO: Implement the logic to update the Warframe Res RM
    }

    private static void updateWarframeRivenChanges(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(() -> new RivenDispositionUpdates().upRivenTrend())
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "紫卡倾向变动 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "紫卡倾向变动 更新失败！", false);
                    }
                });
    }

    private static void updateWarframeSister(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        CompletableFuture.supplyAsync(WarframeDataSource::getLichSisterWeapons)
                .thenAccept(items -> {
                    if (items != -1) {
                        bot.sendMsg(event, "信条/赤毒武器 已更新：" + items + " 条数据！", false);
                    } else {
                        bot.sendMsg(event, "信条/赤毒武器 更新失败！", false);
                    }
                });
    }

//    private static void updateWarframeTar(Bot bot, AnyMessageEvent event) {
//        bot.sendMsg(event, "已发布任务，正在更新！", false);
//        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
//            if (flag) {
//                CompletableFuture.allOf(CompletableFuture.supplyAsync(WarframeDataSource::initTranslation)).thenAccept(items ->
//                        bot.sendMsg(event, "翻译数据，已更新： " + items + " 条数据！", false)
//                );
//            } else {
//                bot.sendMsg(event, "翻译数据，更新失败！", false);
//            }
//        });
//    }

    private static void updateJar(Bot bot, AnyMessageEvent event) {
        log.debug("Updating the jar file");
        //当前版本
        String lodeVersion = "v" + SystemInfoUtils.getJarVersion();
        log.debug("Current version：{}", lodeVersion);
        //判断当前版本是否与最新版本相等
        if (GitHubUtil.isLatestVersion(lodeVersion)) {
            log.debug("The current version is the latest version, no need to update!");
            bot.sendMsg(event, "当前已经是最新版本，无需更新！", false);
        } else {
            //最新版本
            String latestTagName = GitHubUtil.getLatestTagName();
            log.debug("Latest version：{}", latestTagName);
            bot.sendMsg(event, "当前版本：" + lodeVersion + "，最新版本：" + latestTagName + "，正在准备更新！", false);
            String body = GitHubUtil.getBody();
            log.debug("Latest version update log：{}", body);
            bot.sendMsg(event, "最新版本更新日志：" + body, false);
            bot.sendMsg(event, "正在更新，请稍后...\n若长时间没有反应，请手动更新或者回退版本。\n备份文件在backup目录中。", false);
            if (GitHubUtil.getLatestZip("./tmp/NyxBot.jar")) {
                log.debug("Update successful!");
                bot.sendMsg(event, "更新成功，正在重启！", false);
                UpdateJarUtils.restartUpdate("NyxBot.jar");
            } else {
                bot.sendMsg(event, "更新失败，请手动更新！", false);
            }

        }
    }
}
