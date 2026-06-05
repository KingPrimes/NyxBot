package com.nyx.bot.task;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import com.nyx.bot.utils.FileUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.Arbitration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 仲裁独立通知任务
 * 仲裁数据来自独立 API，不入 ChangeDetector 体系
 * 每小时整点后 1 分钟触发，检测当前仲裁是否变化
 */
@Slf4j
@Component
public class ArbitrationNotificationTask {

    private static final String LAST_ID_FILE = "./data/arbitration_last_id";

    private final ArbitrationCache arbitrationCache;
    private final MissionSubscribeRepository subscribeRepo;
    private final BotContainer botContainer;
    private final DrawImagePlugin drawImagePlugin;

    public ArbitrationNotificationTask(ArbitrationCache arbitrationCache,
                                       MissionSubscribeRepository subscribeRepo,
                                       BotContainer botContainer,
                                       DrawImagePlugin drawImagePlugin) {
        this.arbitrationCache = arbitrationCache;
        this.subscribeRepo = subscribeRepo;
        this.botContainer = botContainer;
        this.drawImagePlugin = drawImagePlugin;
    }

    /**
     * 每小时第 1 分钟执行，给 API 1 分钟更新缓冲
     */
    @Scheduled(cron = "0 1 * * * *")
    public void checkAndNotify() {
        try {
            Optional<Arbitration> current = arbitrationCache.getArbitration();
            if (current.isEmpty()) {
                log.debug("当前无仲裁数据，跳过通知");
                return;
            }

            Arbitration arb = current.get();
            String currentId = arb.getId();

            String lastId = readLastNotifiedId();
            if (currentId.equals(lastId)) {
                log.debug("仲裁未变化 [id:{}]", currentId);
                return;
            }

            log.info("仲裁已更新 [id:{}] [node:{}] [type:{}]", currentId, arb.getNode(), arb.getType());

            List<MissionSubscribe> subscriptions = subscribeRepo.findSubscriptions(SubscribeType.ARBITRATION);
            if (subscriptions.isEmpty()) {
                log.debug("没有仲裁订阅者，跳过通知");
                writeLastNotifiedId(currentId);
                return;
            }

            byte[] image = drawImagePlugin.drawArbitrationImage(arb);

            for (MissionSubscribe sub : subscriptions) {
                try {
                    // 过滤用户规则（missionType 匹配当前仲裁任务模式）
                    List<Long> matchedUsers = sub.getUsers().stream()
                            .filter(u -> u.getCheckTypes().stream()
                                    .anyMatch(r -> r.getSubscribe() == SubscribeType.ARBITRATION
                                            && (r.getMissionTypeEnum() == null
                                            || r.getMissionTypeEnum().getName().equals(arb.getType()))))
                            .map(MissionSubscribeUser::getUserId)
                            .toList();

                    if (matchedUsers.isEmpty()) {
                        continue;
                    }

                    Bot bot = botContainer.robots.get(sub.getSubBotUid());
                    if (bot == null) {
                        log.warn("Bot {} 不存在，跳过仲裁通知", sub.getSubBotUid());
                        continue;
                    }

                    ArrayMsgUtils msg = ArrayMsgUtils.builder();
                    matchedUsers.forEach(msg::at);
                    msg.text("仲裁已更新！\n")
                            .text("📍 " + arb.getNode() + "\n")
                            .text("🎯 " + arb.getType() + "\n")
                            .text("⚔️ " + arb.getEnemyName() + "\n")
                            .img(image);

                    bot.sendGroupMsg(sub.getSubGroup(), msg.buildCQ(), false);
                    log.info("仲裁通知发送成功 [group:{}] [users:{}]", sub.getSubGroup(), matchedUsers.size());
                } catch (Exception e) {
                    log.error("仲裁通知发送失败 [group:{}]", sub.getSubGroup(), e);
                }
            }

            writeLastNotifiedId(currentId);
        } catch (Exception e) {
            log.error("仲裁通知任务执行异常", e);
        }
    }

    private String readLastNotifiedId() {
        try {
            String content = FileUtils.readFileToString(LAST_ID_FILE);
            return content != null ? content.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void writeLastNotifiedId(String id) {
        try {
            FileUtils.writeFile(LAST_ID_FILE, id.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("写入仲裁 lastId 失败", e);
        }
    }
}
