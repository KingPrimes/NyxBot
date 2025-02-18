package com.nyx.bot.repo.impl.warframe;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class MissionSubscribeService {


    private final MissionSubscribeRepository subscribeRepository;

    private final BotContainer botContainer;

    private final Executor asyncExecutor;

    @Autowired
    public MissionSubscribeService(MissionSubscribeRepository subscribeRepository, BotContainer botContainer, @Qualifier("myAsync") Executor asyncExecutor){
        this.asyncExecutor = asyncExecutor;
        this.subscribeRepository = subscribeRepository;
        this.botContainer = botContainer;
    }

    public void handleUpdate(SubscribeEnums type, GlobalStates newData) {
        List<MissionSubscribe> subscriptions = subscribeRepository.findSubscriptions(type);

        subscriptions.parallelStream().forEach(subscribe -> subscribe.getUsers().stream()
                .filter(user -> isUserSubscribed(user, type))
                .forEach(user -> CompletableFuture.runAsync(() ->
                                buildAndSendMessage(subscribe, user, type, newData),
                        asyncExecutor
                )));
    }

    private boolean isUserSubscribed(MissionSubscribeUser user, SubscribeEnums type) {
        return user.getCheckTypes().stream()
                .anyMatch(check -> check.getSubscribe() == type);
    }

    private void buildAndSendMessage(MissionSubscribe subscribe,
                                     MissionSubscribeUser user,
                                     SubscribeEnums type,
                                     GlobalStates data) {
        try {
            Bot bot = botContainer.robots.get(subscribe.getSubBotUid());
            if (bot == null) return;

            Msg msg = Msg.builder()
                    .at(user.getUserId())
                    .text("您订阅的 " + type.getNAME() + " 已更新！\n");

            appendContentByType(msg, type, data, subscribe, user);
            bot.sendGroupMsg(subscribe.getSubGroup(), msg.build(), false);
        } catch (Exception e) {
            log.error("通知发送失败 [group:{}] [user:{}] [type:{}]",
                    subscribe.getSubGroup(), user.getUserId(), type, e);
        }
    }

    private void appendContentByType(Msg builder,
                                     SubscribeEnums type,
                                     GlobalStates data,
                                     MissionSubscribe subscribe,
                                     MissionSubscribeUser user) {
        switch (type) {
            case FISSURES:
                appendFissures(builder, data.getFissures(), subscribe, user);
                break;
            case NEWS:
                appendNews(builder, data.getNews());
                break;
            case ALERTS:
                addSystemImage(builder, type, subscribe, user, data.getAlerts());
                break;
            case INVASIONS:
                addSystemImage(builder, type, subscribe, user, data.getInvasions());
                break;
            case CETUS_CYCLE:
                builder.text(I18nUtils.message("warframe.up.cetusCycle") + DateUtils.getDiff(data.getCetusCycle().getExpiry(), new Date()));
                break;
            case VOID:
                if (data.getVoidTrader().getInventory().isEmpty() && !data.getVoidTrader().getActive()) {
                    builder.text(I18nUtils.message("warframe.up.voidOut"));
                    addSystemImage(builder, type, subscribe, user, null);
                } else {
                    builder.text(I18nUtils.message("warframe.up.voidIn"));
                    addSystemImage(builder, type, subscribe, user, null);
                }
                break;
            // 其他类型处理...
            default:
                addSystemImage(builder, type, subscribe, user, null);
        }
    }

    private void appendNews(Msg builder,
                            List<GlobalStates.News> news) {
        news.forEach(n -> {
            if (!n.getTranslations().getZh().isEmpty()) {
                builder.text(n.getTranslations().getZh());
            } else {
                builder.text(n.getTranslations().getEn());
            }
            builder.text("\n").img(n.getImageLink()).text(n.getLink()).text("\n");
        });
    }

    private void appendFissures(Msg builder,
                                List<GlobalStates.Fissures> fissures,
                                MissionSubscribe subscribe,
                                MissionSubscribeUser user) {
        List<GlobalStates.Fissures> filtered = filterFissures(fissures, user);
        if (!filtered.isEmpty()) {
            builder.text("裂隙信息：\n");
            addSystemImage(builder, SubscribeEnums.FISSURES, subscribe, user, filtered);
        }
    }

    private List<GlobalStates.Fissures> filterFissures(List<GlobalStates.Fissures> fissures,
                                                       MissionSubscribeUser user) {
        return user.getCheckTypes().stream()
                .filter(check -> check.getSubscribe() == SubscribeEnums.FISSURES)
                .flatMap(check -> fissures.stream()
                        .filter(f -> Objects.equals(f.getTierNum(), check.getTierNum()) &&
                                f.getMissionType().contains(check.getMissionTypeEnum().get())))
                .toList();
    }

    /**
     * 根据订阅类型返回图片Url后缀地址
     *
     * @param enums 订阅类型
     * @return 图片Url后缀地址
     */
    private String gestural(SubscribeEnums enums) {
        switch (enums) {
            case ALERTS -> {
                return "postSubAlertsImage";
            }
            case ARBITRATION -> {
                return "getArbitrationImage";
            }
            case DAILY_DEALS -> {
                return "postDailyDealsImage";
            }
            case VOID -> {
                return "postVoidImage";
            }
            case CETUS_CYCLE -> {
                return "postAllCycleImage";
            }
            case INVASIONS -> {
                return "postSubInvasionsImage";
            }
            case STEEL_PATH -> {
                return "postSteelPathImage";
            }
            case NIGHTWAVE -> {
                return "postNighTwaveImage";
            }
            case SORTIE -> {
                return "postAssaultImage";
            }
            case ARCHON_HUNT -> {
                return "postArsonHuntImage";
            }
            case DUVIRI_CYCLE -> {
                return "postDuviriCycleImage";
            }
            case FISSURES -> {
                return "postSubscribeFissuresImage";
            }
            default -> {
                return "";
            }
        }
    }

    private void addSystemImage(Msg builder, SubscribeEnums enums, MissionSubscribe subscribe, MissionSubscribeUser user, Object o) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                gestural(enums), new OneBotLogInfoData(
                        subscribe.getSubBotUid(),
                        user.getUserId(),
                        subscribe.getSubGroup(),
                        enums.getNAME(),
                        DateUtils.getDate(),
                        PermissionsEnums.MANAGE,
                        Codes.WARFRAME_SUBSCRIBE,
                        o != null ? JSON.toJSONString(o) : "")
        );
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            builder.imgBase64(body.getFile());
        }
    }

}
