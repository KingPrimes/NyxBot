package com.nyx.bot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.utils.WarframeSubscribe;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.WorldState;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskWarframeStatus {

    @Resource
    ObjectMapper objectMapper;

    @Value("${test.isTest}")
    Boolean test;

    @Async("taskExecutor")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void executeWarframeStatus() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
        if (body.code().is2xxSuccessful() || body.code().is3xxRedirection()) {
            try {
                WorldState worldState = objectMapper.readValue(body.body(), WorldState.class);
                WarframeCache.setWarframeStatus(worldState);
                WarframeSubscribe.isUpdated(worldState);
                log.info("Warframe 状态数据更新成功");
            } catch (Exception e) {
                log.error("Warframe 状态数据解析错误: {}", e.getMessage());
            }
        } else {
            log.error("Warframe 状态数据错误Code: {} - Body:{}", body.code(), body.body());
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
