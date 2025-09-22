package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.entity.exprot.NightWave;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.repo.exprot.RelicsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.utils.RelicsImportUtil;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Rollback(false)
@Slf4j
public class TestInitData {

    @Resource
    WeaponsRepository repository;


    @Resource
    NodesRepository nodesRepository;

    @Resource
    NightWaveRepository nightwaveRepository;

    @Resource
    RewardPoolRepository rewardPoolRepository;

    @Resource
    StateTranslationRepository str;


    @Resource
    RelicsRepository relicsRepository;

    @Resource
    EntityManager entityManager;
    @Test
    void initAlias() {
        WarframeDataSource.getRivenTrend();
    }


    @Test
    @Transactional
    void testInitRelics() {
        Long start = System.currentTimeMillis();
        RelicsImportUtil util = new RelicsImportUtil(str, relicsRepository);
        Integer i1 = util.importRelicsData("./data/export/ExportRelicArcane_zh.json");
        log.info("已导入{}条数据", i1);
        Long end = System.currentTimeMillis();
        log.info("耗时{}ms", end - start);
        assertThat(i1).isGreaterThan(0);
    }
    @Test
    void testInitExprot() {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(WarframeDataSource::severExportFiles)
                .thenRunAsync(() -> new WarframeDataSource().initStateTranslation())
                .thenRunAsync(() -> new WarframeDataSource().initNodes())
                .thenRunAsync(() -> new WarframeDataSource().initWeapons())
                .thenRun(() -> log.info("数据初始化完成！"));
        completableFuture.join();
    }

    @Test
    void testInitRewardPool() throws FileNotFoundException {
        List<RewardPool> javaList = JSON.parseArray(new FileInputStream("./data/reward_pool.json")).toJavaList(RewardPool.class);
        rewardPoolRepository.saveAll(javaList);
    }

    @Test
    void test() {
        Weapons weapons = repository.findById("/Lotus/Weapons/Infested/InfestedLich/Melee/CodaMire").orElse(new Weapons());
        log.info("{}", weapons.getDamagePerShotList());
        log.info("{}", JSON.toJSONString(weapons));
    }

    @Test
    void testInitNightwave() throws FileNotFoundException {
        List<NightWave> javaList = JSON.parseObject(new FileInputStream("./data/export/ExportSortieRewards_zh.json")).getJSONObject("ExportNightwave").getJSONArray("challenges").toJavaList(NightWave.class);
        nightwaveRepository.saveAll(javaList);
    }

    @Test
    void testInitWeapons(){
        new WarframeDataSource().initWeapons();
    }

    @Test
    void testInitExprot2() {
        log.info("Customs:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportCustoms_zh.json", "ExportCustoms", StateTypeEnum.ALL)));
        log.info("Drones:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportDrones_zh.json", "ExportDrones", StateTypeEnum.ALL)));
        log.info("Flavour:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportFlavour_zh.json", "ExportFlavour", StateTypeEnum.ALL)));
        log.info("Gear:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportGear_zh.json", "ExportGear", StateTypeEnum.GEAR)));
        log.info("Keys:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportKeys_zh.json", "ExportKeys", StateTypeEnum.KEYS)));
        log.info("RelicArcane:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportRelicArcane_zh.json", "ExportRelicArcane", StateTypeEnum.ALL)));
        log.info("Resources:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportResources_zh.json", "ExportResources", StateTypeEnum.RESOURCES)));
        log.info("Sentinels:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportSentinels_zh.json", "ExportSentinels", StateTypeEnum.SENTINELS)));
        log.info("Other:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportSortieRewards_zh.json", "ExportOther", StateTypeEnum.OTHER)));
        log.info("Mods:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportUpgrades_zh.json", "ExportUpgrades", StateTypeEnum.MODS)));
        log.info("Warframes:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportWarframes_zh.json", "ExportWarframes", StateTypeEnum.WARFRAMES)));
        log.info("Weapons:{}", JSON.toJSONString(parsingExportJsonToStateTranslation("./data/export/ExportWeapons_zh.json", "ExportWeapons", StateTypeEnum.WEAPONS)));
    }

    @Test
    void initNodes() throws FileNotFoundException {
        List<Nodes> nodesList = JSON.parseArray(new FileInputStream("./data/nodes.json")).toJavaList(Nodes.class);
        nodesRepository.saveAll(nodesList);
    }

    @Test
    void initStateTranslationData() throws FileNotFoundException {
        List<StateTranslation> javaList = JSON.parseArray(new FileInputStream("./data/state_translation.json")).toJavaList(StateTranslation.class)
                .stream()
                .peek(s -> {
                    s.setType(StateTypeEnum.RESOURCES);
                    Arrays.stream(StateTypeEnum.values())
                            .filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY()))
                            .findFirst()
                            .ifPresentOrElse(s::setType, () -> s.setType(StateTypeEnum.RESOURCES));
                })
                .toList();
        str.saveAll(javaList);
    }

    @Test
    void initStateTranslation() throws FileNotFoundException {
        List<StateTranslation> stateTranslationList = new ArrayList<>();
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportCustoms_zh.json", "ExportCustoms", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportDrones_zh.json", "ExportDrones", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportFlavour_zh.json", "ExportFlavour", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportGear_zh.json", "ExportGear", StateTypeEnum.GEAR));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportKeys_zh.json", "ExportKeys", StateTypeEnum.KEYS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportRelicArcane_zh.json", "ExportRelicArcane", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportResources_zh.json", "ExportResources", StateTypeEnum.RESOURCES));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportSentinels_zh.json", "ExportSentinels", StateTypeEnum.SENTINELS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportSortieRewards_zh.json", "ExportOther", StateTypeEnum.OTHER));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportUpgrades_zh.json", "ExportUpgrades", StateTypeEnum.MODS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportWarframes_zh.json", "ExportWarframes", StateTypeEnum.WARFRAMES));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation("./data/export/ExportWeapons_zh.json", "ExportWeapons", StateTypeEnum.WEAPONS));
        FileUtils.writeFile("./data/st.json", JSON.toJSONString(stateTranslationList));
        List<StateTranslation> javaList = JSON.parseArray(new FileInputStream("./data/state_translation.json")).toJavaList(StateTranslation.class)
                .stream()
                .peek(s -> {
                    s.setType(StateTypeEnum.RESOURCES);
                    Arrays.stream(StateTypeEnum.values())
                            .filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY()))
                            .findFirst()
                            .ifPresentOrElse(s::setType, () -> s.setType(StateTypeEnum.RESOURCES));
                })
                .toList();
        //log.info("javaList: {}", JSON.toJSONString(javaList));
        stateTranslationList.addAll(javaList);
        str.saveAll(stateTranslationList);

        List<RewardPool> rps = JSON.parseArray(new FileInputStream("./data/reward_pool.json")).toJavaList(RewardPool.class);
        rewardPoolRepository.saveAll(rps);
    }

    List<Nodes> parsingExportJsonToNodes(String exportPath, String key) {
        JSONObject object;
        try {
            object = JSON.parseObject(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return object.getList(key, Nodes.class);
    }

    /**
     * 解析导出文件到  List<StateTranslation>
     *
     * @param exportPath 文件路径
     * @return List<StateTranslation>
     */
    List<StateTranslation> parsingExportJsonToStateTranslation(String exportPath, String key, StateTypeEnum typeEnum) {
        JSONObject object;
        try {
            object = JSON.parseObject(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return object.getList(key, StateTranslation.class).stream().filter(s -> !s.getName().isEmpty()).peek(s -> {
                    s.setType(StateTypeEnum.RESOURCES);
                    Arrays.stream(StateTypeEnum.values())
                            .filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY()))
                            .findFirst()
                            .ifPresentOrElse(s::setType, () -> s.setType(typeEnum));
                })
                .toList();
    }

    /**
     * 获取DE官方数据文件并保存到本地
     *
     * @param languagesKey 语言
     * @param path         lzma 文件保存路径
     * @param outPath      解压文件保存路径
     * @param exportPath   索引数据保存路径 ./data/export/%s
     */
    void severExportFiles(String languagesKey, String path, String outPath, String exportPath) {
        Boolean files = getExportLZMAFiles(languagesKey, path);
        if (!files) {
            return;
        }
        if (ZipUtils.unLzma(path, outPath)) {
            List<String> keys = FileUtils.readFileToList(outPath);
            for (String key : keys) {
                if (key.contains("ExportRecipes") || key.contains("ExportFusionBundles")) {
                    continue;
                }
                getExportFiles(key, exportPath.formatted(StringUtils.substring(key, 0, key.indexOf("!"))));
            }
        }
    }

    /**
     * 获取DE官方数据文件并保存到本地
     */
    void severExportFiles() {
        String path = "./data/lzma/index_zh.txt.lzma";
        String outPath = "./data/lzma/index_zh.txt";
        String exportPath = "./data/export/%s";
        severExportFiles("zh", path, outPath, exportPath);
    }

    /**
     * 获取LZMA 索引文件
     *
     * @param path - 文件保存路径
     * @param key  语言
     */
    Boolean getExportLZMAFiles(String key, String path) {
        return HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted(key), path);
    }

    /**
     * 根据索引获取数据
     *
     * @param key  索引
     * @param path 文件保存路径
     */
    Boolean getExportFiles(String key, String path) {
        return HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_MANIFESTS.formatted(key), path);
    }

}
