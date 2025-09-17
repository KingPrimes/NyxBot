package com.nyx.bot;

import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class NyxBotApplicationTest {
    public static void main(String[] args) {
        // 使用自定义的环境实例启动 Spring 应用
        SpringApplication app = new SpringApplication(NyxBotApplicationTest.class);
        app.setEnvironment(HandOff.getEnv(args));
        app.run(args);
        WarframeDataSource.initWarframeStatus();
    }

}
