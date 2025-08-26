package com.nyx.bot.task;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.config.TokenKeys;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.plugin.warframe.utils.WarframeSubscribe;
import com.nyx.bot.repo.warframe.TokenKeysRepository;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class TaskWarframeStatus {
     @Value("${test.isTest}")
    Boolean test;

    @Async("taskExecutor")
    @Scheduled(cron = "0/120 * * * * ?")
    public void executeWarframeStatus() {
        if (!test) {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
            if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                try {
                    WorldState worldState = JSON.parseObject(body.getBody(), WorldState.class);
                    WarframeCache.setWarframeStatus(worldState);
                    WarframeSubscribe.isUpdated(worldState);
                } catch (JSONException e) {
                    log.error("Warframe 状态数据解析错误: {}", e.getMessage());
                }
            } else {
                log.error("Warframe 状态数据错误: {}", body.getBody());
            }
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

}
