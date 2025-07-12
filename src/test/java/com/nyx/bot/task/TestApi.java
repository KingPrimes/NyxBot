package com.nyx.bot.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.StateTranslation;
import com.nyx.bot.entity.warframe.exprot.Nodes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.StateTranslationRepository;
import com.nyx.bot.repo.warframe.exprot.NodesRepository;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.res.worldstate.*;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestApi {

    @Resource
    StateTranslationRepository str;

    @Resource
    NodesRepository nodesRepository;

    FileInputStream state = new FileInputStream("./data/state.json");
    WorldState worldState = JSON.parseObject(state, WorldState.class);

    public TestApi() throws FileNotFoundException {
    }

    @Test
    void testGetWorldState() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            WorldState worldState = JSONObject.parseObject(body.getBody(), WorldState.class);
            log.info(JSON.toJSONString(worldState));
        }
    }

    /**
     * 测试每日特惠
     */
    @Test
    void testStateDailyDeals() {
        worldState.getDailyDeals().stream().peek(d -> {
            d.setItem(str.findByUniqueName(StringUtils.getLastThreeSegments(d.getItem())).orElse(new StateTranslation()).getName());
        }).findFirst().ifPresent(d -> log.info(JSON.toJSONString(d)));
    }

    /**
     * 测试入侵
     */
    @Test
    void testStateInvasions() {
        List<Invasion> list = worldState.getInvasions().stream()
                .peek(d -> {
                            Nodes nodes = nodesRepository.findById(d.getNode()).orElse(new Nodes());
                            d.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                            List<Reward.Item> items = d.getDefenderReward().getCountedItems()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .peek(i -> i.setName(str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).orElse(new StateTranslation()).getName()))
                                    .toList();
                            d.getDefenderReward().setCountedItems(items);

                            d.setAttackerReward(d.getAttackerReward().stream()
                                    .filter(Objects::nonNull)
                                    .peek(r -> {
                                        r.setCountedItems(
                                                r.getCountedItems()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .peek(i -> i.setName(str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).orElse(new StateTranslation()).getName()))
                                                        .toList()
                                        );
                                    }).toList());
                        }
                ).toList();
        log.info(JSON.toJSONString(list));
    }

    /**
     * 测试裂隙任务
     */
    @Test
    void testStateActiveMissions() {
        List<ActiveMission> hard = worldState.getActiveMissions().stream()
                .filter(m -> Objects.nonNull(m.getHard()) && m.getHard())
                .peek(m -> {
                    Nodes nodes = nodesRepository.findById(m.getNode()).orElse(new Nodes());
                    m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
        log.info("ActiveMissions Hard:{}",JSON.toJSONString(hard));
        List<ActiveMission> list = worldState.getActiveMissions().stream()
                .filter(m -> !Objects.nonNull(m.getHard()))
                .peek(m -> {
                    Nodes nodes = nodesRepository.findById(m.getNode()).orElse(new Nodes());
                    m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();

        log.info("ActiveMissions:{}",JSON.toJSONString(list));
    }

    /**
     * 测试九重天裂隙任务
     */
    @Test
    void testVoidStorms() {
        List<VoidStorms> list = worldState.getVoidStorms().stream()
                .peek(m -> {
                    Nodes nodes = nodesRepository.findById(m.getNode()).orElse(new Nodes());
                    m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                }).sorted(Comparator.comparing(VoidStorms::getVoidEnum)).toList();
        log.info(JSON.toJSONString(list));
    }

    /**
     * 测试虚空商人
     */
    //          "ItemType": "/Lotus/StoreItems/Types/Keys/MummyQuestKeyBlueprint",
    //          "zh": "Inaros 之沙 蓝图",
    //          "dec": "帮 Baro Ki'Teer 突袭一座古老的火星古墓并盗取一件神秘的宝物。",

    //          "ItemType": "/Lotus/StoreItems/Types/BoosterPacks/BaroTreasureBox",
    //          "zh": "虚空余货",
    //          "dec": "随机出售一件未贴标的库存余货，恕不提供挑选。每位顾客限购一个。",
    @Test
    void testVoidTraders() {
        List<VoidTrader> list = worldState.getVoidTraders().stream()
                .peek(v -> {
                    Nodes nodes = nodesRepository.findById(v.getNode()).orElse(new Nodes());
                    v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                    v.setManifest(v.getManifest()
                            .stream()
                            .peek(m ->
                                    m.setItem(
                                            str.findByUniqueName(StringUtils.getLastThreeSegments(m.getItem()))
                                                    .orElse(new StateTranslation()).getName()
                                    )
                            )
                            .toList()
                    );
                }).toList();
        log.info(JSON.toJSONString(list));
    }

    /**
     * 测试突击任务
     */
    @Test
    void testSorties() {
        List<Sortie> list = worldState.getSorties().stream()
                .peek(s -> {
                    s.setVariants(s.getVariants().stream()
                            .peek(v -> {
                                Nodes node = nodesRepository.findById(v.getNode()).orElse(new Nodes());
                                v.setNode(node.getName() + "(" + node.getSystemName() + ")");
                            }).toList());
                }).toList();
        log.info("Sorties:{}",JSON.toJSONString(list));
    }

    /**
     * 执刑官猎杀
     */
    @Test
    void testLiteSorties() {
        List<LiteSorite> list = worldState.getLiteSorties().stream()
                .peek(s -> {
                    s.setMissions(s.getMissions().stream()
                            .peek(v -> {
                                Nodes node = nodesRepository.findById(v.getNode()).orElse(new Nodes());
                                v.setNode(node.getName() + "(" + node.getSystemName() + ")");
                            }).toList());
                }).toList();
        log.info("LiteSorties:{}",JSON.toJSONString(list));
    }

    @Test
    void testUnzip() throws IOException {
        Boolean zh = HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted("zh"), "./data/lzma/index_zh.txt.lzma");
        if (zh) {
            ZipUtils.unLzma("./data/lzma/index_zh.txt.lzma", "./data/lzma/index_zh.txt");
        }
    }


}
