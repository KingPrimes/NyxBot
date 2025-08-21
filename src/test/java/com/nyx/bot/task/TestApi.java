package com.nyx.bot.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.StateTranslation;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.plugin.warframe.utils.SyndicateMissionsUtils;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.StateTranslationRepository;
import com.nyx.bot.repo.warframe.exprot.NightwaveRepository;
import com.nyx.bot.repo.warframe.exprot.NodesRepository;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.res.enums.SyndicateEnum;
import com.nyx.bot.res.worldstate.*;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestApi {

    @Resource
    StateTranslationRepository str;

    @Resource
    TranslationService trans;
    @Resource
    NodesRepository nodesRepository;

    @Resource
    NightwaveRepository nightwaveRepository;

    FileInputStream state = new FileInputStream("./data/state5.json");
    WorldState worldState = JSON.parseObject(state, WorldState.class);

    public TestApi() throws FileNotFoundException {
    }

    static Map<String, String> compareTheHashAndSave(List<String> keys) {
        String keysHashPath = "./data/keys.json";
        Map<String, String> collect = keys.stream()
                .map(key -> key.split("!", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
        try {
            Map<String, String> map = JSON.parseObject(new FileInputStream(keysHashPath)).toJavaObject(Map.class);
            FileUtils.writeFile(keysHashPath, JSON.toJSONString(collect));
            return collect.entrySet().stream()
                    .filter(e -> !Objects.equals(e.getValue(), map.get(e.getKey())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        } catch (FileNotFoundException e) {
            FileUtils.writeFile(keysHashPath, JSON.toJSONString(collect));
            return collect;
        }
    }

    @Test
    void testWorldState() {
        log.info(JSON.toJSONString(worldState));
    }

    @Test
    void testGetWorldState() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            FileUtils.writeFile("./data/state5.json", body.getBody());
        }
    }

    /**
     * 测试集团赏金任务
     */
    @Test
    void testSyndicateMissions() {
        SyndicateMission syndicateMissions = SyndicateMissionsUtils.getSyndicateMissions(worldState.getSyndicateMissions(), SyndicateEnum.CetusSyndicate);
        log.info(JSON.toJSONString(syndicateMissions));
    }

    /**
     * 测试每日特惠
     */
    @Test
    void testStateDailyDeals() {
        worldState.getDailyDeals().stream().peek(i -> {
            str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName()));
        }).findFirst().ifPresent(d -> log.info(JSON.toJSONString(d, JSONWriter.Feature.PrettyFormatWith4Space)));
    }

    /**
     * 测试双衍王境
     */
    @Test
    void testDuviriCycle() {
        DuviriCycle duviriCycle = worldState.getDuviriCycle();
        List<EndlessXpChoices> list = duviriCycle.getChoices().stream().peek(c -> {
            if (c.getCategory().equals(EndlessXpChoices.Category.EXC_HARD)) {
                c.setChoices(c.getChoices().stream().map(s -> trans.enToZh(s)).toList());
            }
        }).toList();
        duviriCycle.setChoices(list);
        log.info(JSON.toJSONString(duviriCycle, JSONWriter.Feature.PrettyFormatWith4Space));
    }

    /**
     * 测试入侵
     */
    @Test
    void testStateInvasions() {
        List<Invasion> list = worldState.getInvasions().stream()
                .filter(i -> !i.getCompleted())
                .peek(d -> {
                            nodesRepository.findById(d.getNode())
                                    .ifPresent(nodes -> d.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            List<Reward.Item> items = d.getDefenderReward().getCountedItems()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .peek(i -> {
                                        str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                                    })
                                    .toList();
                            d.getDefenderReward().setCountedItems(items);

                            d.setAttackerReward(d.getAttackerReward().stream()
                                    .filter(Objects::nonNull)
                                    .peek(r -> {
                                        r.setCountedItems(
                                                r.getCountedItems()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .peek(i -> {
                                                            str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                                                        })
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
                    nodesRepository.findById(m.getNode()).ifPresent(nodes -> {
                        m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                    });
                }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
        log.info("ActiveMissions Hard:{}", JSON.toJSONString(hard));
        List<ActiveMission> list = worldState.getActiveMissions().stream()
                .filter(m -> !Objects.nonNull(m.getHard()))
                .peek(m -> {
                    nodesRepository.findById(m.getNode()).ifPresent(nodes -> {
                        m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                    });
                }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();

        log.info("ActiveMissions:{}", JSON.toJSONString(list));
    }

    /**
     * 测试九重天裂隙任务
     */
    @Test
    void testVoidStorms() {
        List<ActiveMission> list = worldState.getVoidStorms().stream()
                .map(v -> {
                    ActiveMission am = new ActiveMission();
                    nodesRepository.findById(v.getNode()).ifPresentOrElse(nodes -> {
                        am.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                        am.setMissionType(nodes.getMissionType());
                    }, () -> {
                        am.setNode(v.getNode());
                    });
                    am.set_id(v.get_id());
                    am.setActivation(v.getActivation());
                    am.setExpiry(v.getExpiry());
                    am.setModifier(v.getVoidEnum());
                    return am;
                })
                .sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
        log.info(JSON.toJSONString(list));
    }

    /**
     * 测试电波
     */
    @Test
    void testSeasonInfo() {
        SeasonInfo seasonInfo = worldState.getSeasonInfo();
        seasonInfo.setActiveChallenges(seasonInfo.getActiveChallenges().stream().peek(c -> {
            nightwaveRepository.findById(c.getChallenge()).ifPresent(c::setNightwave);
        }).toList());
        log.info(JSON.toJSONString(seasonInfo));
    }

    /**
     * 钢铁轮换
     */
    @Test
    void testSteelPathOffering() {
        log.info(JSON.toJSONString(worldState.getSteelPath()));
    }


    /**
     * 测试虚空商人
     */
    @Test
    void testVoidTraders() {
        List<VoidTrader> list = worldState.getVoidTraders().stream()
                .peek(v -> {
                    nodesRepository.findById(v.getNode())
                            .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                    if (v.getManifest() != null && !v.getManifest().isEmpty()) {
                        v.setManifest(v.getManifest()
                                .stream()
                                .peek(i -> {
                                    str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName()));
                                })
                                .toList()
                        );
                    }
                }).toList();
        log.info(JSON.toJSONString(list));
    }

    // 瓦奇娅
    // ScheduleInfo 时间表信息
    @Test
    void testPrimeVaultTrader() {
        List<PrimeVaultTrader> primeVaultTraders = worldState.getPrimeVaultTraders()
                .stream()
                .peek(p -> nodesRepository.findById(p.getNode())
                        .ifPresent(nodes -> p.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")")))
                .peek(p -> {
                    p.setManifest(p.getManifest().stream().peek(m -> {
                        str.findByUniqueName(StringUtils.getLastThreeSegments(m.getItemType()))
                                .ifPresent(s -> m.setItemType(s.getName()));
                    }).collect(Collectors.toList()));
                    p.setEvergreenManifest(p.getEvergreenManifest().stream().peek(m -> {
                        str.findByUniqueName(StringUtils.getLastThreeSegments(m.getItemType()))
                                .ifPresent(s -> m.setItemType(s.getName()));
                    }).collect(Collectors.toList()));
                    p.setScheduleInfos(p.getScheduleInfos().stream().peek(m -> {
                        str.findByUniqueName(StringUtils.getLastThreeSegments(m.getItem()))
                                .ifPresent(s -> m.setItem(s.getName()));
                    }).collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
        log.info(JSON.toJSONString(primeVaultTraders));
    }

    @Test
    void testFlashSale() {
        List<FlashSale> flashSales = worldState.getFlashSales()
                .stream()
                .peek(f -> {
                    str.findByUniqueName(StringUtils.getLastThreeSegments(f.getTypeName()))
                            .ifPresent(s -> f.setTypeName(s.getName()));
                })
                .toList();
        log.info(JSON.toJSONString(flashSales));
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
                                nodesRepository.findById(v.getNode())
                                        .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            }).toList());
                }).toList();
        log.info("Sorties:{}", JSON.toJSONString(list, JSONWriter.Feature.PrettyFormatWith4Space));
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
                                nodesRepository.findById(v.getNode())
                                        .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            }).toList());
                }).toList();
        log.info("LiteSorties:{}", JSON.toJSONString(list, JSONWriter.Feature.PrettyFormatWith4Space));
    }

    /**
     * 测试活动任务
     */
    @Test
    void testGoals() {
        List<Goal> list = worldState.getGoals().stream().peek(g -> {
            nodesRepository.findById(g.getNode()).ifPresent(nodes -> g.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));

            if (g.getMissionKeyName().isEmpty() && !g.getScoreLocTag().isEmpty()) {
                str.findByUniqueName(g.getScoreLocTag()).ifPresent(s -> {
                    g.setMissionKeyName(s.getName());
                    g.setDesc(s.getDescription());
                });
            } else {
                str.findByUniqueName(g.getMissionKeyName()).ifPresent(s -> {
                    g.setMissionKeyName(s.getName());
                    g.setDesc(s.getDescription());
                });
            }
            List<Reward> rewardList = new ArrayList<>(g.getInterimRewards().stream().peek(r -> {
                r.setCountedItems(r.getCountedItems().stream().peek(i -> {
                    str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                }).toList());
                r.setItems(r.getItems().stream().map(i -> {
                    StateTranslation stateTranslation = str.findByUniqueName(StringUtils.getLastThreeSegments(i)).orElse(null);
                    if (stateTranslation == null) {
                        return i;
                    }
                    return stateTranslation.getName();
                }).toList());
            }).toList());

            Reward reward = g.getReward();
            if (reward != null) {
                reward.setCountedItems(reward.getCountedItems().stream().peek(i -> {
                    str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                }).toList());
                reward.setItems(reward.getItems().stream().map(i -> {
                    StateTranslation s = str.findByUniqueName(StringUtils.getLastThreeSegments(i)).orElse(null);
                    if (s == null) {
                        return i;
                    }
                    return s.getName();
                }).toList());
                g.setReward(reward);
                rewardList.add(reward);
            }
            Reward breward = g.getBonusReward();
            if (breward != null) {
                breward.setCountedItems(breward.getCountedItems().stream().peek(i -> {
                    str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                }).toList());
                breward.setItems(breward.getItems().stream().map(i -> {
                    StateTranslation s = str.findByUniqueName(StringUtils.getLastThreeSegments(i)).orElse(null);
                    if (s == null) {
                        return i;
                    }
                    return s.getName();
                }).toList());
                g.setBonusReward(breward);
                g.setInterimRewards(rewardList);
            }
            rewardList.add(breward);
        }).toList();
        log.info("Goals:{}", JSON.toJSONString(list));
    }

    @Test
    void testUnzip() {
        Boolean zh = HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted("zh"), "./data/lzma/index_zh.txt.lzma");
        log.info("文件获取状态:{}", zh);
        if (zh) {
            Boolean b = ZipUtils.unLzma("./data/lzma/index_zh.txt.lzma", "./data/lzma/index_zh.txt");
            log.info("文件解压状态:{}", b);
        }
    }

    @Test
    void testCompareKeysHash() {
        List<String> keys = FileUtils.readFileToList("./data/lzma/index_zh.txt");
        Map<String, String> compareTheHashAndSave = compareTheHashAndSave(keys);
        log.info("KeysHash:{}", compareTheHashAndSave);
    }

}
