package com.nyx.bot.data;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.cache.WarframeCache;
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
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.service.*;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.WorldState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings("all")
public class WarframeDataSource {

    static final String EXPORT_PATH = "./data/export/%s";
    static final String LAMA_PATH = "./data/lzma/index_zh.txt.lzma";
    static final String INDEX_PATH = "./data/lzma/index_zh.txt";
    static ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);
    StateTranslationRepository str = SpringUtils.getBean(StateTranslationRepository.class);
    NodesRepository nodesRepository = SpringUtils.getBean(NodesRepository.class);
    WeaponsRepository weaponsRepository = SpringUtils.getBean(WeaponsRepository.class);
    RewardPoolRepository rewardPoolRepository = SpringUtils.getBean(RewardPoolRepository.class);

    public static void init() {
        log.info("开始初始化数据！");

        // 优化后的任务编排：
        // 1. 首先执行 initStateTranslation（可能被其他任务依赖）
        // 2. 然后并行执行所有数据库更新任务
        // 3. 注意：虽然添加了同步锁，但仍保持单线程执行数据库批量更新任务以提高性能
        CompletableFuture
                .supplyAsync(WarframeDataSource::severExportFiles)
                .thenAccept(flag -> {
                    if (!flag) {
                        log.error("获取导出数据文件失败！请检查网络环境");
                        throw new RuntimeException("获取导出数据文件失败");
                    }
                })
                // 步骤1: 初始化状态翻译数据（优先级最高，可能被其他任务依赖）
                .thenCompose(ignore ->
                        CompletableFuture.runAsync(() -> new WarframeDataSource().initStateTranslation())
                                // 步骤2: 并行执行所有数据初始化任务
                                .thenRunAsync(() -> {
                                    // 数据库批量更新任务组（这些任务各自已有同步锁保护）
                                    CompletableFuture<Void> dbUpdateTasks = CompletableFuture.allOf(
                                            CompletableFuture.runAsync(WarframeDataSource::getAlias),
                                            CompletableFuture.runAsync(WarframeDataSource::getRivenTion),
                                            CompletableFuture.runAsync(WarframeDataSource::getRivenTionAlias),
                                            CompletableFuture.runAsync(WarframeDataSource::getRivenAnalyseTrend)
                                    );

                                    // 其他数据初始化任务组（读取操作为主，可以完全并行）
                                    CompletableFuture<Void> otherTasks = CompletableFuture.allOf(
                                            CompletableFuture.runAsync(WarframeDataSource::initWarframeStatus),
                                            CompletableFuture.runAsync(WarframeDataSource::getEphemeras),
                                            CompletableFuture.runAsync(WarframeDataSource::initOrdersItemsData),
                                            CompletableFuture.runAsync(WarframeDataSource::getLichSisterWeapons),
                                            CompletableFuture.runAsync(WarframeDataSource::getRivenWeapons),
                                            CompletableFuture.runAsync(() -> new WarframeDataSource().initNodes()),
                                            CompletableFuture.runAsync(() -> new WarframeDataSource().initWeapons()),
                                            CompletableFuture.runAsync(() -> new WarframeDataSource().initRewardPool()),
                                            CompletableFuture.runAsync(() -> new WarframeDataSource().initNightWave()),
                                            CompletableFuture.runAsync(WarframeDataSource::getRelics)
                                    );

                                    // 等待所有任务完成
                                    CompletableFuture.allOf(dbUpdateTasks, otherTasks).join();
                                }))
                // 所有任务完成后输出信息
                .thenRun(() -> log.info("数据初始化完成！"))
                // 异常处理
                .exceptionally(ex -> {
                    log.error("初始化过程中发生异常，正在回退操作...", ex);

                    // 回退操作：清理已生成的文件
                    FileUtils.delAllFile("./data");

                    log.error("回退操作完成，程序即将退出。");
                    System.exit(SpringApplication.exit(SpringUtils.getApplicationContext(), () -> -1));
                    return null;
                });
    }

    @SneakyThrows
    public static void initWarframeStatus() {
        String a = FileUtils.readFileToString("./data/arbitration");
        String str = FileUtils.readFileToString("./data/status");
        if (!str.isEmpty()) {
            WorldState worldState = objectMapper.readValue(str, WorldState.class);
            WarframeCache.setWarframeStatus(worldState);
        }
        if (!a.isEmpty()) {
            List arbitration = objectMapper.readValue(Base64.getDecoder().decode(a), List.class);
            ArbitrationCache.setArbitration(arbitration);
        } else {
            ArbitrationCache.reloadArbitration();
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
    public static void getAlias() {
        log.info("开始初始化别名数据！");
        int i = SpringUtils.getBean(AliasService.class).updateAlias();
        log.info("总计更新 Warframe.Alias {} 数据！", i);
    }

    // 紫卡词条
    public static void getRivenTion() {
        log.info("开始初始化 RivenTion 数据！");
        int i = SpringUtils.getBean(RivenTionService.class).updateRivenTion();
        log.info("总计更新 Warframe.RivenTion {} 数据！", i);
    }

    // 紫卡词条别名
    public static void getRivenTionAlias() {
        log.info("开始初始化 RivenTion 别名数据！");
        int i = SpringUtils.getBean(RivenTionAliasService.class).updateRivenTionAlias();
        log.info("总计更新 Warframe.RivenTion.Alias {} 数据！", i);
    }

    //紫卡计算器数据
    public static void getRivenAnalyseTrend() {
        log.info("开始初始化 RivenAnalyseTrend 数据！");
        int r = SpringUtils.getBean(RivenAnalyseTrendService.class).updateRivenAnalyseTrends();
        log.info("总计更新 Warframe.RivenAnalyseTrend {} 数据！", r);
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
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(new FileInputStream(exportPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            JsonNode arrayNode = rootNode.get(key);
            List<StateTranslation> list = objectMapper.convertValue(
                    arrayNode,
                    new TypeReference<List<StateTranslation>>() {
                    }
            );
            return list.stream().filter(s -> !s.getName().isEmpty()).peek(s -> {
                s.setType(StateTypeEnum.RESOURCES);
                Arrays.stream(StateTypeEnum.values()).filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY())).findFirst().ifPresentOrElse(s::setType, () -> s.setType(typeEnum));
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + key + " from " + exportPath, e);
        }
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
            Map<String, String> map = objectMapper.readValue(
                    new FileInputStream(keysHashPath),
                    new TypeReference<Map<String, String>>() {
                    }
            );
            FileUtils.writeFile(keysHashPath, objectMapper.writeValueAsString(collect));
            return collect.entrySet().stream().filter(e -> !Objects.equals(e.getValue(), map.get(e.getKey()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (FileNotFoundException e) {
            try {
                FileUtils.writeFile(keysHashPath, objectMapper.writeValueAsString(collect));
            } catch (Exception ex) {
                throw new RuntimeException("Failed to write keys file", ex);
            }
            return collect;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse keys file", e);
        }
    }

    <T> List<T> parsingExportJson(String exportPath, String key, Class<T> c) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            log.error("{} 路径文件不存在", exportPath);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("解析文件失败: {}", exportPath, e);
            throw new RuntimeException(e);
        }
        try {
            JsonNode arrayNode = rootNode.get(key);
            return objectMapper.convertValue(
                    arrayNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, c)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + key + " from " + exportPath, e);
        }
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
        log.debug("开始初始化 自定义 Lost state_translation.json 翻译 数据！");
        List<StateTranslation> javaList = SpringUtils.getBean(StateTranslationService.class).getStateTranslationsForCnd();
        List<StateTranslation> sts = javaList.stream().peek(s -> {
            Arrays.stream(StateTypeEnum.values())
                    .filter(stateTypeEnum -> s.getUniqueName()
                            .matches(stateTypeEnum.getKEY()))
                    .findFirst()
                    .ifPresentOrElse(s::setType, () -> s.setType(StateTypeEnum.RESOURCES));
        }).toList();
        size = str.saveAll(sts).size();
        log.debug("初始化 自定义 Lost state_translation.json 翻译 数据完成，共{}条", size);
        log.debug("初始化 Lost 翻译 数据完成");
    }

    void initNightWave() {
        log.debug("开始初始化  NightWave 数据！");
        try {
            JsonNode rootNode = objectMapper.readTree(new FileInputStream(EXPORT_PATH.formatted("ExportSortieRewards_zh.json")));
            JsonNode challengesNode = rootNode.get("ExportNightwave").get("challenges");
            List<NightWave> javaList = objectMapper.convertValue(
                    challengesNode,
                    new TypeReference<List<NightWave>>() {
                    }
            );
            int size = SpringUtils.getBean(NightWaveRepository.class).saveAll(javaList).size();
            log.debug("初始化 NightWave 数据完成，共{}条", size);
        } catch (FileNotFoundException e) {
            log.error("ExportSortieRewards_zh.json文件不存在", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("解析 NightWave 数据失败", e);
            throw new RuntimeException(e);
        }
    }

    void initNodes() {
        log.debug("开始初始化  Nodes 数据！");
        List<Nodes> nodesList = parsingExportJson(EXPORT_PATH.formatted("ExportRegions_zh.json"), "ExportRegions", Nodes.class);
        int size = nodesRepository.saveAll(nodesList).size();
        log.debug("初始化 Nodes 数据完成，共{}条", size);
        List<Nodes> nodes = new ArrayList<>();
        log.debug("开始初始化 自定义 Nodes nodes.json 数据！");
        for (String url : ApiUrl.WARFRAME_DATA_SOURCE_NODES) {
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (body.code().is2xxSuccessful()) {
                try {
                    List<Nodes> customNodes = objectMapper.readValue(
                            body.body(),
                            new TypeReference<List<Nodes>>() {
                            }
                    );
                    nodes.addAll(customNodes);
                    break;
                } catch (Exception e) {
                    log.error("解析 Nodes 数据失败: {}", url, e);
                }
            }
        }
        size = nodesRepository.saveAll(nodes).size();
        log.debug("初始化 自定义 Nodes nodes.json 数据完成，共{}条", size);
    }

    void initRewardPool() {
        log.debug("开始初始化 自定义 RewardPool reward_pool.json 数据！");
        List<RewardPool> javaList = new ArrayList<>();
        for (String url : ApiUrl.WARFRAME_DATA_SOURCE_REWARD_POOL) {
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (body.code().is2xxSuccessful()) {
                try {
                    List<RewardPool> pools = objectMapper.readValue(
                            body.body(),
                            new TypeReference<List<RewardPool>>() {
                            }
                    );
                    javaList.addAll(pools);
                    break;
                } catch (Exception e) {
                    log.error("解析 RewardPool 数据失败: {}", url, e);
                }
            }
        }
        int size = rewardPoolRepository.saveAll(javaList).size();
        log.debug("初始化 自定义 RewardPool reward_pool.json 数据完成，共{}条", size);
    }

    void initWeapons() {
        log.debug("开始初始化  Weapons 数据！");
        List<Weapons> weapons = parsingExportJson(EXPORT_PATH.formatted("ExportWeapons_zh.json"), "ExportWeapons", Weapons.class);
        weapons.forEach(w -> w.setEnglishName(w.contEnglishName()));
        int size = weaponsRepository.saveAll(weapons).size();
        log.debug("初始化 Weapons 数据完成，共{}条", size);
    }

}
