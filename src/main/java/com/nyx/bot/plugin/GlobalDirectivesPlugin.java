package com.nyx.bot.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.plugin.help.HelpCode;
import com.nyx.bot.plugin.warframe.code.WarframeCodes;
import com.nyx.bot.utils.CodeUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.CqMatcher;
import com.nyx.bot.utils.onebot.CqParse;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Shiro
@Component
@Slf4j
public class GlobalDirectivesPlugin {

    private static void not(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "该功能暂未实现！", false);
    }

    @AnyMessageHandler
    public void messageDisposeHandler(Bot bot, AnyMessageEvent event) {
        String raw = event.getRawMessage();
        if (CqMatcher.isCqAt(raw)) {
            CqParse build = CqParse.build(raw);
            if (build.getCqAt().get(0).equals(bot.getSelfId())) {
                raw = build.reovmCq().trim();
            }
        }
        if (raw.contains("/")) {
            raw = raw.replaceAll("/", "").trim();
        }
        Codes code = CodeUtils.matchInstructions(raw);
        String finalRaw = raw.toUpperCase();
        Optional.ofNullable(code).ifPresent(codes -> {
            switch (codes) {
                //帮助菜单
                case HELP -> HelpCode.help(bot, event);
                //运行状态
                case CHECK_VERSION -> {
                    HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                            "systemInfo",
                            bot, event);
                    if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                        bot.sendMsg(event,
                                Msg.builder().imgBase64(body.getFile()).build(), false);
                    }
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
                //双衍王境
                case WARFRAME_KING_REALM_ROTATION -> WarframeCodes.duviriCycle(bot, event);
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
                case WARFRAME_RIVEN_DIS_UPDATE_PLUGIN -> WarframeCodes.rivenDisUpdate(bot, event, codes);
                //翻译
                case WARFRAME_TRA_PLUGIN -> not(bot, event);
                // /WM
                case WARFRAME_MARKET_ORDERS_PLUGIN -> WarframeCodes.orders(bot, event, finalRaw, codes);
                // /WR
                case WARFRAME_MARKET_RIVEN_PLUGIN -> WarframeCodes.marketRiven(bot, event, finalRaw, codes);
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
                //金银垃圾
                case WARFRAME_MARKET_GOD_DUMP,WARFRAME_MARKET_SILVER_DUMP -> WarframeCodes.ducat(bot, event, codes);
                //核桃
                case WARFRAME_RELICS_PLUGIN -> not(bot, event);
                //开核桃
                case WARFRAME_OPEN_RELICS_PLUGIN -> not(bot, event);
                //紫卡分析
                case WARFRAME_RIVEN_ANALYSE -> WarframeCodes.ocrRivenCompute(bot, event, codes);
                //订阅处理
                case WARFRAME_SUBSCRIBE -> WarframeCodes.subscribe(bot, event,codes);
            }
        });
    }
}
