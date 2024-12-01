package com.nyx.bot.plugin.admin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SystemInfoUtils;
import com.nyx.bot.utils.UpdateJarUtils;
import com.nyx.bot.utils.gitutils.GitHubUtil;

public class UpdatePlugin {

    /**
     * 执行更新内容
     */
    public static void updatePlugin(Bot bot, AnyMessageEvent event, Codes codes) {
        switch (codes) {
            case UPDATE_HTML:
                updateHtml(bot, event);
            case UPDATE_WARFRAME_RES_MARKET_ITEMS:
                updateWarframeResMarketItems(bot, event);
            case UPDATE_WARFRAME_RES_MARKET_RIVEN:
                updateWarframeResMarketRiven(bot, event);
            case UPDATE_WARFRAME_RES_RM:
                updateWarframeResRm(bot, event);
            case UPDATE_WARFRAME_RIVEN_CHANGES:
                updateWarframeRivenChanges(bot, event);
            case UPDATE_WARFRAME_SISTER:
                updateWarframeSister(bot, event);
            case UPDATE_WARFRAME_TAR:
                updateWarframeTar(bot, event);
            case UPDATE_JAR:
                updateJar(bot, event);
        }
    }


    private static void updateHtml(Bot bot, AnyMessageEvent event) {
        WarframeDataSource.cloneDataSource();
        bot.sendMsg(event, "已发布任务，正在更新！", false);
    }

    private static void updateWarframeResMarketItems(Bot bot, AnyMessageEvent event) {
        WarframeDataSource.getMarket();
        bot.sendMsg(event, "已发布任务，正在更新！", false);
    }

    private static void updateWarframeResMarketRiven(Bot bot, AnyMessageEvent event) {
        WarframeDataSource.getRivenWeapons();
        bot.sendMsg(event, "已发布任务，正在更新！", false);
    }

    private static void updateWarframeResRm(Bot bot, AnyMessageEvent event) {
        // TODO: Implement the logic to update the Warframe Res RM
    }

    private static void updateWarframeRivenChanges(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "已发布任务，正在更新！", false);
        new RivenDispositionUpdates().upRivenTrend();
    }

    private static void updateWarframeSister(Bot bot, AnyMessageEvent event) {
        WarframeDataSource.getWeapons();
        bot.sendMsg(event, "已发布任务，正在更新！", false);
    }

    private static void updateWarframeTar(Bot bot, AnyMessageEvent event) {
        WarframeDataSource.cloneDataSource();
        WarframeDataSource.initTranslation();
        bot.sendMsg(event, "已发布任务，正在更新！", false);
    }

    private static void updateJar(Bot bot, AnyMessageEvent event) {
        //当前版本
        String lodeVersion = "v" + SystemInfoUtils.getJarVersion();
        //判断当前版本是否与最新版本相等
        if (GitHubUtil.isLatestVersion(lodeVersion)) {
            bot.sendMsg(event, "当前已经是最新版本，无需更新！", false);
        } else {
            //最新版本
            String latestTagName = GitHubUtil.getLatestTagName();
            bot.sendMsg(event, "当前版本：" + lodeVersion + "，最新版本：" + latestTagName + "，正在准备更新！", false);
            String body = GitHubUtil.getBody();
            bot.sendMsg(event, "最新版本更新日志：" + body, false);
            bot.sendMsg(event, "正在更新，请稍后...", false);
            String path = "./tmp/NyxBot.jar";
            FileUtils.writeToFile(GitHubUtil.getLatestZip(), path);
            new UpdateJarUtils().restartUpdate("NyxBot.jar");
        }
    }
}
