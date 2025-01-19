package com.nyx.bot.task;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.plugin.warframe.utils.WarframeSubscribe;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskWarframeStatus {

//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Async("taskExecutor")
    @Scheduled(cron = "0/60 * * * * ?")
    public void execute() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_STATUS + "pc");
        GlobalStates.Arbitration arbitration = CacheUtils.getArbitration();
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            GlobalStates states = JSONObject.parseObject(body.getBody(), GlobalStates.class, JSONReader.Feature.SupportSmartMatch);
            if (arbitration != null) {
                states.setArbitration(arbitration);
            }
            WarframeSubscribe.isUpdated(states);
        } else {
            log.info("获取数据失败！");
        }
    }

    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 1/5 * ? ")
    public void executeRivenTrend() {
        if (Math.random() < 0.5) {
            new RivenDispositionUpdates().upRivenTrend();
        }
    }

    /**
     * 定时更新数据
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 1/3 * ? ")
    public void executeDataSourcePullRandom() {
        if (Math.random() < 0.5) {
            WarframeDataSource.init();
        }
    }

//    @Async("taskExecutor")
//    @Scheduled(initialDelay = 1000)
//    public void scheduleRandomTask() {
//        scheduleNextTask();
//    }
//
//    private void scheduleNextTask() {
//        Random random = new Random();
//        int randomHour = random.nextInt(24);
//        int randomMinute = random.nextInt(60);
//
//        // 计算下一天的时间
//        Calendar nextDay = Calendar.getInstance();
//        nextDay.add(Calendar.DAY_OF_YEAR, 1);
//        nextDay.set(Calendar.HOUR_OF_DAY, randomHour);
//        nextDay.set(Calendar.MINUTE, randomMinute);
//        nextDay.set(Calendar.SECOND, 0);
//        nextDay.set(Calendar.MILLISECOND, 0);
//
//        // 计算延迟时间
//        long delay = nextDay.getTimeInMillis() - System.currentTimeMillis();
//
//        scheduler.schedule(this::executeTask, delay, TimeUnit.MILLISECONDS);
//    }
//
//    private void executeTask() {
//        // 再次安排下一次任务
//        scheduleNextTask();
//    }

}
