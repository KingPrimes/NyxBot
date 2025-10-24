package com.nyx.bot.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.utils.WarframeSubscribe;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskWarframeStatus {
     @Value("${test.isTest}")
    Boolean test;

    @Async("taskExecutor")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void executeWarframeStatus() {
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

    /**
     * 定时更新数据
     */
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 1/3 * ? ")
    public void executeDataSourcePullRandom() {
        if (Math.random() < 0.5 && !test) {
            WarframeDataSource.init();
        }
    }

}
