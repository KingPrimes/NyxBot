package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.modules.warframe.entity.*;
import com.nyx.bot.modules.warframe.entity.exprot.NightWave;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.repo.*;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.service.*;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.Arbitration;
import io.github.kingprimes.model.WorldState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("all")
public class WarframeDataSource {

    static final String DATA_SOURCE_PATH = JgitUtil.lockPath + "/warframe/";
    static final String EXPORT_PATH = "./data/export/%s";
    static final String LAMA_PATH = "./data/lzma/index_zh.txt.lzma";
    static final String INDEX_PATH = "./data/lzma/index_zh.txt";

    StateTranslationRepository str = SpringUtils.getBean(StateTranslationRepository.class);
    NodesRepository nodesRepository = SpringUtils.getBean(NodesRepository.class);
    WeaponsRepository weaponsRepository = SpringUtils.getBean(WeaponsRepository.class);
    RewardPoolRepository rewardPoolRepository = SpringUtils.getBean(RewardPoolRepository.class);

    public static void init() {
        log.info("开始初始化数据！");

        // 1. 首先执行cloneDataSource
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
                    if (flag) {
                        log.error("初始化数据模板，失败！请检查网络环境");
                        throw new RuntimeException("克隆数据模板失败");
                    }
                })
                // 使用thenCompose确保severExportFiles在cloneDataSource完成后执行
                .thenCompose(ignore -> CompletableFuture.supplyAsync(WarframeDataSource::severExportFiles)).thenAccept(flag -> {
                    if (!flag) {
                        log.error("获取导出数据文件失败！请检查网络环境");
                        throw new RuntimeException("获取导出数据文件失败");
                    }
                })
                // 3. severExportFiles完成后执行initStateTranslation
                .thenCompose(ignore -> CompletableFuture.runAsync(() -> new WarframeDataSource().initStateTranslation()))
                // 4. 剩余任务并行执行
                .thenRunAsync(() -> {
                    CompletableFuture.allOf(

                                    CompletableFuture.runAsync(WarframeDataSource::getAlias),
                                    CompletableFuture.runAsync(WarframeDataSource::getRivenTion),
                                    CompletableFuture.runAsync(WarframeDataSource::getRivenTionAlias),
                                    CompletableFuture.runAsync(WarframeDataSource::initTranslation),
                                    CompletableFuture.runAsync(WarframeDataSource::getRivenAnalyseTrend),
                                    CompletableFuture.runAsync(WarframeDataSource::getRivenTrend),


                                    CompletableFuture.runAsync(WarframeDataSource::initWarframeStatus),
                                    CompletableFuture.runAsync(WarframeDataSource::getEphemeras),
                                    CompletableFuture.runAsync(WarframeDataSource::initOrdersItemsData),
                                    CompletableFuture.runAsync(WarframeDataSource::getLichSisterWeapons),
                                    CompletableFuture.runAsync(WarframeDataSource::getRivenWeapons),


                                    CompletableFuture.runAsync(() -> new WarframeDataSource().initNodes()),
                                    CompletableFuture.runAsync(() -> new WarframeDataSource().initWeapons()),
                                    CompletableFuture.runAsync(() -> new WarframeDataSource().initRewardPool()),
                                    CompletableFuture.runAsync(() -> new WarframeDataSource().initNightWave()),
                                    CompletableFuture.runAsync(WarframeDataSource::getRelics))
                            .join();
                })
                // 所有任务完成后输出信息
                .thenRun(() -> log.info("数据初始化完成！"))
                // 异常处理
                .exceptionally(ex -> {
                    log.error("初始化过程中发生异常，正在回退操作...", ex);

                    // 回退操作：清理已生成的文件
                    FileUtils.delAllFile(EXPORT_PATH.formatted("").replace("%s", ""));
                    FileUtils.delAllFile(JgitUtil.lockPath);
                    FileUtils.deleteFile(LAMA_PATH);
                    FileUtils.deleteFile(INDEX_PATH);
                    FileUtils.deleteFile("./data/keys.json");

                    log.error("回退操作完成，程序即将退出。");
                    System.exit(SpringApplication.exit(SpringUtils.getApplicationContext(), () -> -1));
                    return null;
                });
    }

    public static void initWarframeStatus() {
        String a = FileUtils.readFileToString("./data/arbitration");
        String str = FileUtils.readFileToString("./data/status");
        if (!str.isEmpty()) {
            WorldState worldState = JSON.parseObject(str, WorldState.class);
            WarframeCache.setWarframeStatus(worldState);
        }
        if (!a.isEmpty()) {
            List<Arbitration> arbitration = JSON.parseArray(Base64.getDecoder().decode(a), Arbitration.class);
            ArbitrationCache.setArbitration(arbitration);
        } else {
            ArbitrationCache.reloadArbitration();
        }
    }

    private static void sleepSeconds() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //幻纹
    public static Integer getEphemeras() {
        return SpringUtils.getBean(EphemerasService.class).initEphemerasData();
    }


    //Market
    public static Integer initOrdersItemsData() {
        return SpringUtils.getBean(OrdersItemsService.class).initOrdersItemsData();
    }

    //赤毒武器/信条武器
    public static Integer getLichSisterWeapons() {
        return SpringUtils.getBean(LichSisterWeaponsService.class).initLichSisterWeaponsData();
    }

    //紫卡武器
    public static Integer getRivenWeapons() {
        return SpringUtils.getBean(RivenItemsService.class).initRivenItemsData();
    }

    // 遗物
    public static Integer getRelics() {
        return SpringUtils.getBean(RelicsService.class).initRelicsData(EXPORT_PATH.formatted("ExportRelicArcane_zh.json"));
    }

    //别名

    @SneakyThrows
    public static Integer getAlias() {
        log.info("开始初始化别名数据！");
        List<Alias> aliasList = JSON.parseArray(new File(DATA_SOURCE_PATH + "alias.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(Alias.class);
        AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);

        Map<String, Alias> allMap = createMap(aliasR.findAll(), Alias::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, Alias> uniqueTranslations = createMap(aliasList, Alias::getEquation, (oldVal, newVal) -> oldVal);

        List<Alias> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(Alias::new).toList();

        log.info("总计更新 Warframe.Alias {} 数据！", aliasR.saveAll(list).size());

        return list.size();
    }

    // 紫卡词条
    @SneakyThrows
    public static Integer getRivenTion() {
        log.info("开始初始化 RivenTion 数据！");
        List<RivenTion> rivenTions = JSON.parseArray(new File(DATA_SOURCE_PATH + "market_riven_tion.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTion.class);
        RivenTionRepository records = SpringUtils.getBean(RivenTionRepository.class);

        Map<String, RivenTion> allMap = createMap(records.findAll(), RivenTion::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTion> uniqueTranslations = createMap(rivenTions, RivenTion::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTion> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(RivenTion::new).toList();

        log.info("总计更新 Warframe.RivenTion {} 数据！", records.saveAll(list).size());
        return list.size();
    }

    // 紫卡词条别名
    @SneakyThrows
    public static Integer getRivenTionAlias() {
        log.info("开始初始化 RivenTion 别名数据！");
        List<RivenTionAlias> rivenTionAliases = JSON.parseArray(new File(DATA_SOURCE_PATH + "market_riven_tion_alias.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTionAlias.class);
        RivenTionAliasRepository repository = SpringUtils.getBean(RivenTionAliasRepository.class);

        Map<String, RivenTionAlias> allMap = createMap(repository.findAll(), RivenTionAlias::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTionAlias> uniqueTranslations = createMap(rivenTionAliases, RivenTionAlias::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTionAlias> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(RivenTionAlias::new).toList();

        log.info("总计更新 Warframe.RivenTion.Alias {} 数据！", repository.saveAll(list).size());
        return list.size();
    }

    //翻译
    @SneakyThrows
    public static Integer initTranslation() {
        log.info("开始初始化 翻译 数据！");
        List<Translation> translations = JSON.parseArray(new File(DATA_SOURCE_PATH + "translation.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(Translation.class);
        TranslationRepository t = SpringUtils.getBean(TranslationRepository.class);

        Map<String, Translation> allMap = createMap(t.findAll(), Translation::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, Translation> uniqueTranslations = createMap(translations, Translation::getEquation, (oldVal, newVal) -> oldVal);

        List<Translation> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(Translation::new).toList();
        log.info("总计更新 Warframe.Translation {} 数据！", t.saveAll(list).size());
        return list.size();
    }

    //紫卡计算器数据
    @SneakyThrows
    public static Integer getRivenAnalyseTrend() {
        log.info("开始初始化 RivenAnalyseTrend 数据！");
        List<RivenAnalyseTrend> translations = JSON.parseArray(new File(DATA_SOURCE_PATH + "riven_analyse_trend.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenAnalyseTrend.class);
        RivenAnalyseTrendRepository r = SpringUtils.getBean(RivenAnalyseTrendRepository.class);

        Map<String, RivenAnalyseTrend> allMap = createMap(r.findAll(), RivenAnalyseTrend::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenAnalyseTrend> uniqueTranslations = createMap(translations, RivenAnalyseTrend::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenAnalyseTrend> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(RivenAnalyseTrend::new).toList();

        log.info("总计更新 Warframe.RivenAnalyseTrend {} 数据！", r.saveAll(list).size());

        return list.size();
    }

    @SneakyThrows
    public static Integer getRivenTrend() {
        log.info("开始初始化 RivenTrend 数据！");
        List<RivenTrend> rt = JSON.parseArray(new File(DATA_SOURCE_PATH + "riven_trend.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTrend.class);
        RivenTrendRepository r = SpringUtils.getBean(RivenTrendRepository.class);

        Map<String, RivenTrend> allMap = createMap(r.findAll(), RivenTrend::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTrend> uniqueTranslations = createMap(rt, RivenTrend::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTrend> list = uniqueTranslations.values().stream().filter(item -> !allMap.containsKey(item.getEquation())).map(RivenTrend::new).toList();

        log.info("总计更新 Warframe.RivenTrend {} 数据！", r.saveAll(list).size());

        return list.size();
    }


    public static Boolean cloneDataSource() {
        log.info("开始初始化数据模板！");
        boolean flag = true;
        for (String url : ApiUrl.DATA_SOURCE_GIT) {
            try {
                log.debug("Clone data:{}", url);
                JgitUtil git = JgitUtil.Build(url, JgitUtil.lockPath);
                git.pull();
                flag = false;
                break;
            } catch (Exception e) {
                log.error("初始化数据模板不正确", e);
            }
        }
        return flag;
    }

    public static Boolean severExportFiles() {
        return severExportFiles("zh", LAMA_PATH, INDEX_PATH, EXPORT_PATH);
    }

    /**
     * 获取DE官方数据文件并保存到本地
     *
     * @param languagesKey 语言
     * @param path         lzma 文件保存路径
     * @param outPath      解压文件保存路径
     * @param exportPath   索引数据保存路径 ./data/export/%s
     */
    private static Boolean severExportFiles(String languagesKey, String path, String outPath, String exportPath) {
        Boolean files = getExportLZMAFiles(languagesKey, path);
        if (!files) {
            return false;
        }
        Boolean falg = true;
        if (ZipUtils.unLzma(path, outPath)) {
            List<String> keys = FileUtils.readFileToList(outPath);
            Map<String, String> compared = compareTheHashAndSave(keys);
            if (compared.isEmpty()) {
                log.debug("Lzma 数据无变化，无需获取更新！");
                return true;
            }
            for (String key : keys) {
                if (key.contains("ExportRecipes") || key.contains("ExportFusionBundles")) {
                    continue;
                }
                falg = getExportFiles(key, exportPath.formatted(StringUtils.substring(key, 0, key.indexOf("!"))));
            }
        }
        return falg;
    }

    /**
     * 获取LZMA 索引文件
     *
     * @param path - 文件保存路径
     * @param key  语言
     */
    static Boolean getExportLZMAFiles(String key, String path) {
        return HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted(key), path);
    }

    /**
     * 解析导出文件到  List<StateTranslation>
     *
     * @param exportPath 文件路径
     * @return List<StateTranslation>
     */
    static List<StateTranslation> parsingExportJsonToStateTranslation(String exportPath, String key, StateTypeEnum typeEnum) {
        JSONObject object;
        try {
            object = JSON.parseObject(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return object.getList(key, StateTranslation.class).stream().filter(s -> !s.getName().isEmpty()).peek(s -> {
            s.setType(StateTypeEnum.RESOURCES);
            Arrays.stream(StateTypeEnum.values()).filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY())).findFirst().ifPresentOrElse(s::setType, () -> s.setType(typeEnum));
        }).toList();
    }

    /**
     * 根据索引获取数据
     *
     * @param key  索引
     * @param path 文件保存路径
     */
    static Boolean getExportFiles(String key, String path) {
        log.debug("ExportFiles URL:{} Path:{}", key, path);
        return HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_MANIFESTS.formatted(key), path);
    }

    static <T, K> Map<K, T> createMap(Collection<T> items, Function<T, K> keyMapper, BinaryOperator<T> mergeFunction) {
        return items.stream().collect(Collectors.toMap(keyMapper, Function.identity(), mergeFunction));
    }

    /**
     * 对比Lzma数据的Hash值并保存，返回不同的Hash值
     *
     * @param keys Lzma读取的列表
     * @return 不同的Hash值
     */
    static Map<String, String> compareTheHashAndSave(List<String> keys) {
        log.debug("开始对比 Lzma 数据的 Hash 值并保存！");
        String keysHashPath = "./data/keys.json";
        Map<String, String> collect = keys.stream().map(key -> key.split("!", 2)).filter(parts -> parts.length == 2).collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));
        try {
            Map<String, String> map = JSON.parseObject(new FileInputStream(keysHashPath)).toJavaObject(Map.class);
            FileUtils.writeFile(keysHashPath, JSON.toJSONString(collect));
            return collect.entrySet().stream().filter(e -> !Objects.equals(e.getValue(), map.get(e.getKey()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (FileNotFoundException e) {
            FileUtils.writeFile(keysHashPath, JSON.toJSONString(collect));
            return collect;
        }
    }

    <T> List<T> parsingExportJson(String exportPath, String key, Class<T> c) {
        JSONObject object;
        try {
            object = JSON.parseObject(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            log.error("{} 路径文件不存在", exportPath);
            throw new RuntimeException(e);
        }
        return object.getList(key, c);
    }

    void initStateTranslation() {
        log.debug("开始初始化 Lost 翻译 数据！");
        List<StateTranslation> stateTranslationList = new ArrayList<>();
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportCustoms_zh.json"), "ExportCustoms", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportDrones_zh.json"), "ExportDrones", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportFlavour_zh.json"), "ExportFlavour", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportGear_zh.json"), "ExportGear", StateTypeEnum.GEAR));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportKeys_zh.json"), "ExportKeys", StateTypeEnum.KEYS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportRelicArcane_zh.json"), "ExportRelicArcane", StateTypeEnum.ALL));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportResources_zh.json"), "ExportResources", StateTypeEnum.RESOURCES));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportSentinels_zh.json"), "ExportSentinels", StateTypeEnum.SENTINELS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportSortieRewards_zh.json"), "ExportOther", StateTypeEnum.OTHER));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportUpgrades_zh.json"), "ExportUpgrades", StateTypeEnum.MODS));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportWarframes_zh.json"), "ExportWarframes", StateTypeEnum.WARFRAMES));
        stateTranslationList.addAll(parsingExportJsonToStateTranslation(EXPORT_PATH.formatted("ExportWeapons_zh.json"), "ExportWeapons", StateTypeEnum.WEAPONS));
        int size = str.saveAll(stateTranslationList).size();
        log.debug("初始化 Lost 翻译 数据完成，共{}条", size);
        try {
            log.debug("开始初始化 自定义 Lost state_translation.json 翻译 数据！");
            List<StateTranslation> javaList = JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "state_translation.json"), JSONReader.Feature.SupportSmartMatch).toJavaList(StateTranslation.class).stream().peek(s -> {
                s.setType(StateTypeEnum.RESOURCES);
                Arrays.stream(StateTypeEnum.values()).filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY())).findFirst().ifPresentOrElse(s::setType, () -> s.setType(StateTypeEnum.RESOURCES));
            }).toList();
            size = str.saveAll(javaList).size();
            log.debug("初始化 自定义 Lost state_translation.json 翻译 数据完成，共{}条", size);
        } catch (FileNotFoundException e) {
            log.error("state_translation.json文件不存在");
        }
        log.debug("初始化 Lost 翻译 数据完成");
    }

    void initNightWave() {
        log.debug("开始初始化  NightWave 数据！");
        List<NightWave> javaList = null;
        try {
            javaList = JSON.parseObject(new FileInputStream(EXPORT_PATH.formatted("ExportSortieRewards_zh.json"))).getJSONObject("ExportNightwave").getJSONArray("challenges").toJavaList(NightWave.class);
            int size = SpringUtils.getBean(NightWaveRepository.class).saveAll(javaList).size();
            log.debug("初始化 NightWave 数据完成，共{}条", size);
        } catch (FileNotFoundException e) {
            log.error("ExportSortieRewards_zh.json文件不存在", e);
            throw new RuntimeException(e);
        }
    }

    void initNodes() {
        log.debug("开始初始化  Nodes 数据！");
        List<Nodes> nodesList = parsingExportJson(EXPORT_PATH.formatted("ExportRegions_zh.json"), "ExportRegions", Nodes.class);
        int size = nodesRepository.saveAll(nodesList).size();
        log.debug("初始化 Nodes 数据完成，共{}条", size);
        try {
            log.debug("开始初始化 自定义 Nodes nodes.json 数据！");
            List<Nodes> nodes = JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "nodes.json")).toJavaList(Nodes.class);
            size = nodesRepository.saveAll(nodes).size();
            log.debug("初始化 自定义 Nodes nodes.json 数据完成，共{}条", size);
        } catch (FileNotFoundException e) {
            log.error("nodes.json文件不存在", e);
            throw new RuntimeException(e);
        }
    }

    void initRewardPool() {
        try {
            log.debug("开始初始化 自定义 RewardPool reward_pool.json 数据！");
            List<RewardPool> javaList = JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "reward_pool.json")).toJavaList(RewardPool.class);
            int size = rewardPoolRepository.saveAll(javaList).size();
            log.debug("初始化 自定义 RewardPool reward_pool.json 数据完成，共{}条", size);
        } catch (FileNotFoundException e) {
            log.error("reward_pool.json文件不存在", e);
            throw new RuntimeException(e);
        }
    }

    void initWeapons() {
        log.debug("开始初始化  Weapons 数据！");
        List<Weapons> weapons = parsingExportJson(EXPORT_PATH.formatted("ExportWeapons_zh.json"), "ExportWeapons", Weapons.class);
        int size = weaponsRepository.saveAll(weapons).size();
        log.debug("初始化 Weapons 数据完成，共{}条", size);
    }

}
