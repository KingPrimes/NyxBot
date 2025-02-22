package com.nyx.bot.repo.impl.warframe;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.*;
import com.nyx.bot.exception.ServiceException;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserRepository;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MissionSubscribeService {


    private final MissionSubscribeRepository subscribeRepository;

    private final BotContainer botContainer;

    private final MissionSubscribeUserRepository userRepo;

    private final MissionSubscribeUserCheckTypeRepository checkTypeRepo;

    @Autowired
    public MissionSubscribeService(MissionSubscribeRepository subscribeRepository, BotContainer botContainer, MissionSubscribeUserRepository userRepo, MissionSubscribeUserCheckTypeRepository checkTypeRepo) {
        this.subscribeRepository = subscribeRepository;
        this.botContainer = botContainer;
        this.userRepo = userRepo;
        this.checkTypeRepo = checkTypeRepo;
    }

    /**
     * 删除订阅组
     * @param id 订阅组ID
     */
    @Transactional
    public void deleteSubscribeGroup(Long id) {
        Optional<MissionSubscribe> group = subscribeRepository.findById(id);

        group.ifPresentOrElse(g->{
            if(!g.getUsers().isEmpty()){
                throw new ServiceException("存在关联用户，无法删除订阅组");
            }
            subscribeRepository.delete(g);
        },()->{
            throw new ServiceException("订阅组不存在");
        });
    }

    /**
     * 删除订阅用户
     * @param userId 用户ID
     */
    @Transactional
    public void deleteSubscribeUser(Long userId) {
        MissionSubscribeUser user = userRepo.findById(userId)
                .orElseThrow(() -> new ServiceException("用户不存在"));

        // 检查检查类型关联
        if (!user.getCheckTypes().isEmpty()) {
            throw new ServiceException("存在关联类型，无法删除用户");
        }
        userRepo.delete(user);
    }

    /**
     * 删除检查类型
     * @param checkTypeId 检查类型ID
     */
    @Transactional
    public void deleteCheckType(Long checkTypeId) {
        MissionSubscribeUserCheckType checkType = checkTypeRepo.findById(checkTypeId)
                .orElseThrow(() -> new ServiceException("检查类型不存在"));
        checkTypeRepo.delete(checkType);
    }


    /**
     * 处理更新
     * @param type 更新类型
     * @param newData 新数据
     */
    public void handleUpdate(SubscribeEnums type, GlobalStates newData) {
        List<MissionSubscribe> subscriptions = subscribeRepository.findSubscriptions(type);
        subscriptions.parallelStream().forEach(subscribe -> subscribe.getUsers().stream()
                .filter(user -> isUserSubscribed(user, type))
                .forEach(user -> CompletableFuture.runAsync(() ->
                        buildAndSendMessage(subscribe, user, type, newData)
                )));
    }

    /**
     * 判断用户是否订阅了该类型
     * @param user 用户
     * @param type 类型
     * @return true->订阅 false->未订阅
     */
    private boolean isUserSubscribed(MissionSubscribeUser user, SubscribeEnums type) {
        return user.getCheckTypes().stream()
                .anyMatch(check -> check.getSubscribe() == type);
    }

    /**
     * 构建并发送消息
     * @param subscribe 订阅组
     * @param user 用户
     * @param type 类型
     * @param data 数据
     */
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

    /**
     * 构建消息内容
     * @param builder 消息构建器
     * @param type 类型
     * @param data 数据
     * @param subscribe 订阅组
     * @param user 用户
     */
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

    /**
     * 构造新闻消息
     * @param builder 消息构建器
     * @param news 新闻数据
     */
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

    /**
     * 构造裂隙消息
     * @param builder 消息构建器
     * @param fissures 裂隙数据
     * @param subscribe 订阅组
     * @param user 用户
     */
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

    /**
     * 过滤裂隙数据
     * @param fissures 裂隙数据
     * @param user 用户
     * @return 过滤后的裂隙数据
     */
    private List<GlobalStates.Fissures> filterFissures(List<GlobalStates.Fissures> fissures,
                                                       MissionSubscribeUser user) {
        return user.getCheckTypes().stream()
                .filter(check -> check.getSubscribe() == SubscribeEnums.FISSURES)
                .flatMap(check -> fissures.stream()
                        .filter(f -> {
                            if (check.getTierNum() != 0 && !check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return Objects.equals(f.getTierNum(), check.getTierNum()) &&
                                        f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (!check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (check.getTierNum() != 0) {
                                return Objects.equals(f.getTierNum(), check.getTierNum());
                            }
                            return true;
                        }))
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

    /**
     * 添加系统图片
     * @param builder 消息构建器
     * @param enums 类型
     * @param subscribe 订阅组
     * @param user 用户
     * @param o 数据
     */
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
