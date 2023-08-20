package com.nyx.bot.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.plugin.warframe.code.WarframeCodes;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.CodeUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Shiro
@Component
public class GlobalDirectivesPlugin {


    @AnyMessageHandler
    public void messageDispose(Bot bot, AnyMessageEvent event) {
        AsyncUtils.me().execute(()->{
            async(bot, event);
        }, AsyncBeanName.ANYMESSAGEEVENT);
    }

    //异步执行
    public void async(Bot bot,AnyMessageEvent event){
        String raw = event.getRawMessage();
        Codes code = CodeUtils.matchInstructions(raw);
        Optional.ofNullable(code).ifPresent(codes -> {
            switch (codes) {
                //帮助菜单
                case HELP,TYPE_CODE -> HelpCode.help(bot,event);
                //检查版本
                case CHECK_VERSION -> {}
                //更新HTML
                case UPDATE_HTML -> {}
                //更新WM物品
                case UPDATE_RES_MARKET_ITEMS -> {}
                //更新WM紫卡
                case UPDATE_RES_MARKET_RIVEN -> {}
                //更新RM紫卡
                case UPDATE_RES_RM -> {}
                //更新紫卡倾向变动
                case UPDATE_RIVEN_CHANGES -> {}
                //更新信条
                case UPDATE_SISTER -> {}
                //更新翻译
                case UPDATE_TAR -> {}
                //更新版本
                case UPDATE_JAR -> {}
                //开
                case SWITCH_OPEN_WARFRAME,SWITCH_OPEN_MUSIC,SWITCH_OPEN_IMAGE,SWITCH_OPEN_IMAGE_NSFW,SWITCH_OPEN_CHAT_GPT,SWITCH_OPEN_EXPRESSION,SWITCH_OPEN_DRAWING -> {}
                //关
                case SWITCH_OFF_WARFRAME,SWITCH_OFF_MUSIC,SWITCH_OFF_IMAGE,SWITCH_OFF_IMAGE_NSFW,SWITCH_OFF_CHAT_GPT,SWITCH_OFF_EXPRESSION,SWITCH_OFF_DRAWING -> {}
                //点歌
                case MUSIC -> {}
                //涩图
                case IMAGE -> {}
                //鉴图
                case IMAGE_NSFW -> {}
                //CHAT
                case CHAT_GPT -> {}
                //表情包
                case EXPRESSION_CAPO -> {}
                case EXPRESSION_EMAIL_FUNNY -> {}
                case EXPRESSION_SPIRITUAL_PILLARS -> {}

                //Warframe
                //突击
                case WARFRAME_ASSAULT_PLUGIN -> {}
                //执刑官猎杀
                case WARFRAME_ARSON_HUNT_PLUGIN -> {}
                //奸商
                case WARFRAME_VOID_PLUGIN -> {}
                //仲裁
                case WARFRAME_ARBITRATION_PLUGIN -> {}
                //钢铁
                case WARFRAME_STEEL_PATH_PLUGIN -> {}
                //每日特惠
                case WARFRAME_DAILY_DEALS_PLUGIN -> {}
                //入侵
                case WARFRAME_INVASIONS_PLUGIN -> {}
                //裂缝
                case WARFRAME_FISSURES_PLUGIN -> {}
                //九重天
                case WARFRAME_FISSURES_EMPYREAN_PLUGIN -> {}
                //钢铁
                case WARFRAME_FISSURES_PATH_PLUGIN -> {}
                //平原
                case WARFRAME_ALL_CYCLE_PLUGIN -> {}
                //电波
                case WARFRAME_NIGH_WAVE_PLUGIN -> {}
                //倾向变动
                case WARFRAME_RIVEN_DIS_UPDATE_PLUGIN -> {}
                //翻译
                case WARFRAME_TRA_PLUGIN ->{}
                // /WM
                case WARFRAME_MARKET_ORDERS_PLUGIN -> {}
                // /WR
                case WARFRAME_MARKET_RIVEN_PLUGIN -> {}
                // RM
                case WARFRAME_RIVEN_MARKET_PLUGIN -> not(bot, event);
                // CD
                case WARFRAME_CD_PLUGIN -> {}
                // XT
                case WARFRAME_XT_PLUGIN -> {}
                // /WIKI
                case WARFRAME_WIKI_PLUGIN -> {}
                // 佩兰
                case WARFRAME_SISTER_PLUGIN -> {}
                //金垃圾
                case WARFRAME_MARKET_GOD_DUMP -> {}
                //银垃圾
                case WARFRAME_MARKET_SILVER_DUMP -> {}
                //核桃
                case WARFRAME_RELICS_PLUGIN -> {}
                //开核桃
                case WARFRAME_OPEN_RELICS_PLUGIN -> {}
                //紫卡分析
                case WARFRAME_RIVEN_ANALYSE -> not(bot, event);
                //订阅处理
                case WARFRAME_SUBSCRIBE -> {}
            }
        });
        //WarframeCodes.subscribe(bot,event,raw.replaceAll(codes.getStr(),""));
    }

    private static void not(Bot bot,AnyMessageEvent event){
        bot.sendMsg(event,"该功能暂未实现！",false);
    }

}
