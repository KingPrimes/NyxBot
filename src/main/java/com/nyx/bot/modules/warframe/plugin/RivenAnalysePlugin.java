package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.riven_calculation.RivenAttributeCompute;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 紫卡分析
 */
@Shiro
@Component
@Slf4j
public class RivenAnalysePlugin {

    private static final ConcurrentHashMap<String, PendingRequest> PENDING_REQUESTS = new ConcurrentHashMap<>();
    private static final long PENDING_TIMEOUT_MS = 60_000;
    /**
     * 超时通知调度器，单线程 daemon 避免阻止 JVM 退出
     */
    private static final ScheduledExecutorService TIMEOUT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "riven-pending-timeout");
        t.setDaemon(true);
        return t;
    });
    private final DrawImagePlugin drawImagePlugin;
    private final RivenAttributeCompute rivenAttributeCompute;
    public RivenAnalysePlugin(DrawImagePlugin drawImagePlugin, RivenAttributeCompute rivenAttributeCompute) {
        this.drawImagePlugin = drawImagePlugin;
        this.rivenAttributeCompute = rivenAttributeCompute;
    }

    private static String buildKey(AnyMessageEvent event) {
        return event.getGroupId() + ":" + event.getUserId();
    }

    private static void clearPending(AnyMessageEvent event) {
        PENDING_REQUESTS.remove(buildKey(event));
    }

    private static void notifyTimeout(String key, PendingRequest expected) {
        // remove(key, value) 精准匹配：只有当前值未变才删除，避免误删用户重新发起的请求
        if (PENDING_REQUESTS.remove(key, expected)) {
            expected.bot().sendGroupMsg(expected.groupId(), "紫卡截图等待已超时（60 秒），请重新发送指令", false);
        }
    }

    /**
     * 方式一：命令 + 图片（同一消息）→ 直接分析
     * 方式二：仅命令 → 记录等待状态，提示用户补发图片，60秒后超时通知
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_ANALYSE_CMD, at = AtEnum.BOTH)
    public void rivenAnalyse(Bot bot, AnyMessageEvent event) {
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        if (!msgImgUrlList.isEmpty()) {
            clearPending(event);
            byte[] bytes = postRivenAnalyseImage(rivenAttributeCompute.ocrRivenCompute(event));
            SendUtils.send(bot, event, bytes, Codes.WARFRAME_RIVEN_ANALYSE, log);
            return;
        }
        // 只有命令没有图片，记录等待状态并注册超时任务
        String key = buildKey(event);
        PendingRequest req = new PendingRequest(System.currentTimeMillis(), bot, event.getGroupId());
        PENDING_REQUESTS.put(key, req);
        bot.sendMsg(event, "请在 60 秒内发送紫卡截图", false);
        TIMEOUT_SCHEDULER.schedule(() -> notifyTimeout(key, req), PENDING_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 方式三：先发命令后补图片 → 从等待状态中匹配并分析
     * 匹配包含 CQ 图片码的消息（用户单独发送的图片），通过 PENDING_REQUESTS 过滤
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = "\\[CQ:image,", at = AtEnum.BOTH)
    public void pendingRivenHandler(Bot bot, AnyMessageEvent event) {
        String key = buildKey(event);
        PendingRequest req = PENDING_REQUESTS.remove(key);
        if (req == null) {
            return;
        }
        if (System.currentTimeMillis() - req.timestamp() > PENDING_TIMEOUT_MS) {
            return;
        }
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        if (msgImgUrlList.isEmpty()) {
            PENDING_REQUESTS.put(key, req);
            return;
        }
        log.info("群：{} 用户:{} 补发紫卡截图，开始分析", event.getGroupId(), event.getUserId());
        byte[] bytes = postRivenAnalyseImage(rivenAttributeCompute.ocrRivenCompute(event));
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_RIVEN_ANALYSE, log);
    }

    private byte[] postRivenAnalyseImage(List<RivenAnalyseTrendModel> lists) {
        return drawImagePlugin.drawRivenAnalyseTrendImage(lists);
    }

    /**
     * 等待中的紫卡分析请求: key="groupId:userId", value=请求快照
     * 手机端无法同时发送命令+图片，用户先发命令再补图片时从这里匹配
     */
    private record PendingRequest(long timestamp, Bot bot, long groupId) {
    }
}
