package com.nyx.bot;

import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import com.nyx.bot.task.TaskWarframeStatus;
import com.nyx.bot.utils.SpringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class NyxBotApplicationTest {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NyxBotApplicationTest.class);
        app.setEnvironment(HandOff.getEnv(args));
        app.run(args);
        SpringUtils.getBean(WarframeDataSource.class).initWarframeStatus();
        SpringUtils.getBean(TaskWarframeStatus.class).startSchedule();
    }

}
