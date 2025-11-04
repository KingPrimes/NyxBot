package com.nyx.bot.draw;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.data.Constant;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import io.github.kingprimes.PluginManager;
import io.github.kingprimes.model.enums.FactionEnum;
import io.github.kingprimes.model.worldstate.ActiveMission;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestDrawActiveMission {

    PluginManager pm = new PluginManager();

    @Resource
    NodesRepository nodesRepository;

    @Test
    void testDrawActiveMission() throws IOException {
        List<ActiveMission> hard = Constant.WORLD_STATE.getActiveMissions().stream()
                .filter(ActiveMission::getHard)
                .peek(m ->
                        nodesRepository.findById(m.getNode()).ifPresent(nodes -> {
                                    m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                                    m.setFaction(FactionEnum.valueOf(nodes.getFactionName().name()));
                                }
                        )
                )
                .sorted(Comparator.comparing(ActiveMission::getVoidEnum))
                .toList();
        drawActiveMission(hard, "draw_active_mission_hard.png");

        List<ActiveMission> ams = Constant.WORLD_STATE.getActiveMissions().stream()
                .filter(m -> !m.getHard())
                .peek(m ->
                        nodesRepository.findById(m.getNode()).ifPresent(nodes -> {
                                    m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                                    m.setFaction(FactionEnum.valueOf(nodes.getFactionName().name()));
                                }
                        )
                )
                .sorted(Comparator.comparing(ActiveMission::getVoidEnum))
                .toList();
        drawActiveMission(ams, "draw_active_mission.png");
    }

    private void drawActiveMission(List<ActiveMission> activeMissions, String path) throws IOException {
        log.info("ActiveMission:{}", JSON.toJSONString(activeMissions));
        byte[] bytes = pm.getFirstPlugin().drawActiveMissionImage(activeMissions);
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(bytes)), Constant.PNG, new File(Constant.DRAW_PATH.formatted(path)));
    }

}
