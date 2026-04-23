package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.domain.valueobject.SubscriptionCommand;
import com.nyx.bot.modules.warframe.plugin.WarframeTaskSubscribePlugin;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
public class TestTaskSubscribe {

    @Autowired
    WarframeTaskSubscribePlugin warframeTaskSubscribePlugin;
    @Autowired
    SubscriptionApplicationService subscriptionService;


//    @Test
//    public void testUnTaskSubscribe() {
//        String str = "取消订阅9 -22";
//        str = str.replace("取消订阅", "").replace(" ", "").trim();
//        String[] parts = str.split("-");
//        try {
//            SubscribeEnums subscribeType = warframeTaskSubscribePlugin.parseSubscribeType(parts[0]);
//            MissionTypeEnum missionType = parts.length > 1 && !parts[1].isEmpty() ?
//                    warframeTaskSubscribePlugin.parseMissionType(parts[1]) : null;
//            Integer tier = parts.length > 2 && !parts[2].isEmpty() ?
//                    Integer.parseInt(parts[2]) : null;
//
//            // 使用新服务处理取消订阅
//            String result = subscriptionService.unsubscribe(
//                    123456L,
//                    123456789L,
//                    subscribeType,
//                    missionType,
//                    tier
//            );
//            log.info(result);
//        } catch (Exception e) {
//            log.error("错误信息：", e);
//        }
//    }

    @Test
    public void testTaskSubscribe(){
        SubscriptionCommand build = SubscriptionCommand.builder()
                .botUid(123456L)
                .groupId(123456L)
                .groupName("test")
                .userId(123456789L)
                .userName("TestUser")
                .subscribeType(SubscribeEnums.FISSURES)
                .missionType(MissionTypeEnum.MT_ARTIFACT).build();
        subscriptionService.subscribe(build);
    }
}
