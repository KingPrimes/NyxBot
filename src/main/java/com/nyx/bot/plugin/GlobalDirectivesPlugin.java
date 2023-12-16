package com.nyx.bot.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.plugin.help.HelpCode;
import com.nyx.bot.plugin.warframe.code.WarframeCodes;
import com.nyx.bot.utils.CodeUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Shiro
@Component
public class GlobalDirectivesPlugin {

    private static void not(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "该功能暂未实现！", false);
    }

    @AnyMessageHandler
    public void messageDisposeHandler(Bot bot, AnyMessageEvent event) {
        String raw = event.getRawMessage();
        Codes code = CodeUtils.matchInstructions(raw);
        Optional.ofNullable(code).ifPresent(codes -> {
            switch (codes) {
                //帮助菜单
                case HELP, TYPE_CODE -> HelpCode.help(bot, event);
                //运行状态
                case CHECK_VERSION -> {
                }
                //更新操作
                case UPDATE_HTML,
                        UPDATE_WARFRAME_RES_MARKET_ITEMS,
                        UPDATE_WARFRAME_RES_MARKET_RIVEN,
                        UPDATE_WARFRAME_RES_RM,
                        UPDATE_WARFRAME_RIVEN_CHANGES,
                        UPDATE_WARFRAME_SISTER,
                        UPDATE_WARFRAME_TAR,
                        UPDATE_JAR -> {

                }

                //Warframe
                //突击
                case WARFRAME_ASSAULT_PLUGIN -> WarframeCodes.assault(bot, event);
                //执刑官猎杀
                case WARFRAME_ARSON_HUNT_PLUGIN -> WarframeCodes.arsonHun(bot, event);
                //奸商
                case WARFRAME_VOID_PLUGIN -> WarframeCodes.aVoid(bot, event);
                //仲裁
                case WARFRAME_ARBITRATION_PLUGIN -> WarframeCodes.arbitration(bot, event);
                //钢铁
                case WARFRAME_STEEL_PATH_PLUGIN -> WarframeCodes.steelPath(bot, event);
                //每日特惠
                case WARFRAME_DAILY_DEALS_PLUGIN -> WarframeCodes.dailyDeals(bot, event);
                //入侵
                case WARFRAME_INVASIONS_PLUGIN -> WarframeCodes.invasions(bot, event);
                //裂缝
                case WARFRAME_FISSURES_PLUGIN -> WarframeCodes.fissues(bot, event, codes);
                //九重天
                case WARFRAME_FISSURES_EMPYREAN_PLUGIN -> WarframeCodes.fissues(bot, event, codes);
                //钢铁
                case WARFRAME_FISSURES_PATH_PLUGIN -> WarframeCodes.fissues(bot, event, codes);
                //平原
                case WARFRAME_ALL_CYCLE_PLUGIN -> WarframeCodes.allCycle(bot, event);
                //电波
                case WARFRAME_NIGH_WAVE_PLUGIN -> WarframeCodes.nighTwave(bot, event);
                //倾向变动
                case WARFRAME_RIVEN_DIS_UPDATE_PLUGIN -> not(bot, event);
                //翻译
                case WARFRAME_TRA_PLUGIN -> not(bot, event);
                // /WM
                case WARFRAME_MARKET_ORDERS_PLUGIN -> WarframeCodes.orders(bot, event, codes);
                // /WR
                case WARFRAME_MARKET_RIVEN_PLUGIN -> not(bot, event);
                // RM
                case WARFRAME_RIVEN_MARKET_PLUGIN -> not(bot, event);
                // CD
                case WARFRAME_CD_PLUGIN -> not(bot, event);
                // XT
                case WARFRAME_XT_PLUGIN -> not(bot, event);
                // /WIKI
                case WARFRAME_WIKI_PLUGIN -> not(bot, event);
                // 佩兰
                case WARFRAME_SISTER_PLUGIN -> not(bot, event);
                //金垃圾
                case WARFRAME_MARKET_GOD_DUMP -> not(bot, event);
                //银垃圾
                case WARFRAME_MARKET_SILVER_DUMP -> not(bot, event);
                //核桃
                case WARFRAME_RELICS_PLUGIN -> not(bot, event);
                //开核桃
                case WARFRAME_OPEN_RELICS_PLUGIN -> not(bot, event);
                //紫卡分析
                case WARFRAME_RIVEN_ANALYSE -> not(bot, event);
                //订阅处理
                case WARFRAME_SUBSCRIBE -> WarframeCodes.subscribe(bot, event);
            }
        });
    }
}
