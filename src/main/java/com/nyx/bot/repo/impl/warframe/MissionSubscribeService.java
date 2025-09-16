package com.nyx.bot.repo.impl.warframe;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.exception.ServiceException;
import com.nyx.bot.repo.impl.warframe.subscribe.*;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserRepository;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.onebot.Msg;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MissionSubscribeService {


    private final MissionSubscribeRepository subscribeRepository;

    private final BotContainer botContainer;

    private final MissionSubscribeUserRepository userRepo;

    private final MissionSubscribeUserCheckTypeRepository checkTypeRepo;

    private final Map<SubscribeEnums, MessageAppender> appenderMap = Map.of(
            SubscribeEnums.ALERTS, new AlertsMessageAppender(),
            SubscribeEnums.CETUS_CYCLE, new CetusCycleMessageAppender(),
            SubscribeEnums.EVENTS, new EventsMessageAppender(),
            SubscribeEnums.FISSURES, new FissuresMessageAppender(),
            SubscribeEnums.INVASIONS, new InvasionsMessageAppender(),
            SubscribeEnums.NEWS, new NewsMessageAppender(),
            SubscribeEnums.VOID, new VoidMessageAppender()
    );

    @Autowired
    public MissionSubscribeService(MissionSubscribeRepository subscribeRepository, BotContainer botContainer, MissionSubscribeUserRepository userRepo, MissionSubscribeUserCheckTypeRepository checkTypeRepo) {
        this.subscribeRepository = subscribeRepository;
        this.botContainer = botContainer;
        this.userRepo = userRepo;
        this.checkTypeRepo = checkTypeRepo;
    }

    /**
     * 删除订阅组
     *
     * @param id 订阅组ID
     */
    @Transactional
    public void deleteSubscribeGroup(Long id) {
        Optional<MissionSubscribe> group = subscribeRepository.findById(id);

        group.ifPresentOrElse(g -> {
            if (!g.getUsers().isEmpty()) {
                throw new ServiceException("存在关联用户，无法删除订阅组");
            }
            subscribeRepository.delete(g);
        }, () -> {
            throw new ServiceException("订阅组不存在");
        });
    }

    /**
     * 删除订阅用户
     *
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
     *
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
     *
     * @param type    更新类型
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
     *
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
     *
     * @param subscribe 订阅组
     * @param user      用户
     * @param type      类型
     * @param data      数据
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
     *
     * @param builder   消息构建器
     * @param type      类型
     * @param data      数据
     * @param subscribe 订阅组
     * @param user      用户
     */
    private void appendContentByType(Msg builder,
                                     SubscribeEnums type,
                                     GlobalStates data,
                                     MissionSubscribe subscribe,
                                     MissionSubscribeUser user) {
        MessageAppender appender = appenderMap.get(type);
        if (appender != null) {
            appender.appendContent(builder, type, data, subscribe, user);
        } else {
           new MessageAppender(){
               @Override
               public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
                   MessageAppender.super.appendContent(builder, enums, data, subscribe, user);
               }
           };
        }
    }
}
