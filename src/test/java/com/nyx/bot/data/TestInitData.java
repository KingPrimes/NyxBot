package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.StateTranslation;
import com.nyx.bot.entity.warframe.exprot.Nodes;
import com.nyx.bot.entity.warframe.exprot.Weapons;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.repo.warframe.StateTranslationRepository;
import com.nyx.bot.repo.warframe.exprot.NodesRepository;
import com.nyx.bot.repo.warframe.exprot.WeaponsRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestInitData {

    @Resource
    WeaponsRepository repository;

    @Resource
    StateTranslationRepository str;

    @Resource
    NodesRepository nodesRepository;

    @Test
    void initAlias() {
        WarframeDataSource.getRivenTrend();
    }

//    @Test
//    void initTranslation() {
//        WarframeDataSource.initTranslation();
//    }

    @Test
    void test() {
        Weapons weapons = repository.findById("/Lotus/Weapons/Infested/InfestedLich/Melee/CodaMire").orElse(new Weapons());
        log.info("{}", weapons.getDamagePerShotList());
        log.info("{}", JSON.toJSONString(weapons));
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
//        List<Nodes> nodes = parsingExportJsonToNodes("./data/export/ExportRegions_zh.json", "ExportRegions");
//        log.info(JSON.toJSONString(nodes));
//        nodesRepository.saveAll(nodes);
        List<Nodes> nodesList = JSON.parseArray(new FileInputStream("./data/export/nodes.json")).toJavaList(Nodes.class);
        nodesRepository.saveAll(nodesList);
    }

    @Test
    void initStateTranslation() {
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
        //FileUtils.writeFile("./data/st.json", JSON.toJSONString(stateTranslationList));
        str.saveAll(stateTranslationList);
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
