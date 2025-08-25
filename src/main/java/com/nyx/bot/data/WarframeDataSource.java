package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.modules.warframe.entity.*;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.repo.*;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.res.Arbitration;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.ZipUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import com.nyx.bot.utils.http.HttpUtils;
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
        // allOf等待所有任务完成
        CompletableFuture.allOf(
                        // supplyAsync 异步获取数据,返回Boolean值用于下一个线程
                        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource)
                                // 根据上一个线程的返回值进行操作
                                .thenAccept(flag -> {
                                    if (flag) {
                                        log.error("初始化数据模板，失败！请检查网络环境，程序即将自动结束。");
                                        System.exit(SpringApplication.exit(SpringUtils.getApplicationContext(), () -> -1));
                                    } else {
                                        CompletableFuture.allOf(CompletableFuture.runAsync(WarframeDataSource::getAlias)
                                                .thenRunAsync(WarframeDataSource::getRivenTion)
                                                .thenRunAsync(WarframeDataSource::getRivenTionAlias)
                                                //.thenRunAsync(WarframeDataSource::initTranslation)
                                                .thenRunAsync(WarframeDataSource::getRivenAnalyseTrend)
                                                .thenRunAsync(WarframeDataSource::getRivenTrend)).join();
                                    }
                                }),
                        // 初始化网络数据
                        CompletableFuture.runAsync(WarframeDataSource::severExportFiles)
                                .thenRunAsync(WarframeDataSource::initWarframeStatus)
                                .thenRunAsync(WarframeDataSource::getEphemeras)
                                .thenRunAsync(WarframeDataSource::getMarket)
                                .thenRunAsync(WarframeDataSource::getLichSisterWeapons)
                                .thenRunAsync(WarframeDataSource::getRivenWeapons)
                                .thenRunAsync(WarframeDataSource::getRelics)
                ).thenRunAsync(() -> new WarframeDataSource().initStateTranslation())
                .thenRunAsync(() -> new WarframeDataSource().initNodes())
                .thenRunAsync(() -> new WarframeDataSource().initWeapons())
                .thenRunAsync(() -> new WarframeDataSource().initRewardPool())
                .thenRun(() -> log.info("数据初始化完成！"));
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
        log.info("开始获取幻纹信息！");
        int attempts = 0;
        List<Ephemeras> ephemerasList = new ArrayList<>();
        while (attempts < 3) {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒幻纹初始化不正确！未获得数据！尝试在 30 秒后再次获取它！");
                sleepSeconds();
                attempts++;
                continue;
            }
            List<Ephemeras> ephemeras = JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch);
            if (!ephemeras.isEmpty()) {
                ephemerasList.addAll(ephemeras);
                break;
            }
        }
        while (attempts < 3) {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("信条幻纹初始化不正确！未获得数据！尝试在 30 秒后再次获取它！");
                sleepSeconds();
                attempts++;
                continue;
            }
            List<Ephemeras> ephemeras = JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch);
            if (!ephemeras.isEmpty()) {
                ephemerasList.addAll(ephemeras);
                break;
            }
        }
        if (ephemerasList.isEmpty()) {
            log.warn("幻纹初始化不正确！未获得数据！");
            return -1;
        }
        EphemerasRepository repository = SpringUtils.getBean(EphemerasRepository.class);
        if (!repository.findAll().isEmpty()) {
            List<Ephemeras> all = repository.findAll();
            List<Ephemeras> list = ephemerasList.stream()
                    .filter(i -> !all.stream()
                            .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName() + m.getElement(), value -> value))
                            .containsKey(i.getItemName() + i.getUrlName() + i.getElement())
                    ).toList();

            log.info("总更新 Warframe.Ephemeras {} 数据！", list.size());
            return repository.saveAll(list).size();
        } else {
            log.info("总更新 Warframe.Ephemeras {} 数据！", ephemerasList.size());
            return repository.saveAll(ephemerasList).size();
        }
    }


    //Market
    public static Integer getMarket() {
//        log.info("开始初始化Market市场数据!");
//        int attempts = 0;
//        while (attempts < 3) {
//            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
//            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
//                log.warn("Market市场初始化错误！未获得数据！尝试在 30 秒后再次获取它！");
//                sleepSeconds();
//                attempts++;
//                continue;
//            }
//            List<OrdersItems> items = JSON.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("items").toJavaList(OrdersItems.class, JSONReader.Feature.SupportSmartMatch);
//            OrdersItemsRepository ordersItem = SpringUtils.getBean(OrdersItemsRepository.class);
//            if (!ordersItem.findAll().isEmpty()) {
//                List<OrdersItems> all = ordersItem.findAll();
//                List<OrdersItems> list = items.stream()
//                        .filter(item -> !all.stream()
//                                .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName(), value -> value))
//                                .containsKey(item.getItemName() + item.getUrlName())).toList();
//                log.info("总计更新 Warframe.Market {} 数据！", list.size());
//                return ordersItem.saveAll(list).size();
//            } else {
//                log.info("总计更新 Warframe.Market {} 数据！", items.size());
//                return ordersItem.saveAll(items).size();
//
//            }
//        }
//        log.warn("Market市场初始化错误！未获得数据！");
        return -1;
    }

    //赤毒武器/信条武器
    public static Integer getLichSisterWeapons() {
        log.info("开始获取武器信息！");
        int attempts = 0;
        List<LichSisterWeapons> weaponsList = new ArrayList<>();
        while (attempts < 3) {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒武器信息初始化错误！未获得数据！尝试在 30 秒后再次获取它！");
                sleepSeconds();
                attempts++;
                continue;
            }
            List<LichSisterWeapons> weapons = JSONObject.parseObject(body.getBody())
                    .getJSONObject("payload")
                    .getJSONArray("weapons")
                    .toJavaList(LichSisterWeapons.class, JSONReader.Feature.SupportSmartMatch);
            if (!weapons.isEmpty()) {
                weaponsList.addAll(weapons);
                break;
            }
        }
        attempts = 0;
        while (attempts < 3) {
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("信条武器信息初始化错误！未获得数据！尝试在 30 秒后再次获取它！");
                sleepSeconds();
                attempts++;
                continue;
            }
            List<LichSisterWeapons> weapons = JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("weapons").toJavaList(LichSisterWeapons.class, JSONReader.Feature.SupportSmartMatch);
            if (!weapons.isEmpty()) {
                weaponsList.addAll(weapons);
                break;
            }
        }

        if (weaponsList.isEmpty()) {
            log.warn("Market Purple Weapon 初始化错误！未获得数据！");
            return -1;
        }

        LichSisterWeaponsRepository repository = SpringUtils.getBean(LichSisterWeaponsRepository.class);
        if (!repository.findAll().isEmpty()) {
            List<LichSisterWeapons> all = repository.findAll();
            List<LichSisterWeapons> list = weaponsList.stream()
                    .filter(item -> !all.stream()
                            .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName(), value -> value))
                            .containsKey(item.getItemName() + item.getUrlName())).toList();
            log.info("总更新 Warframe.LichSisterWeapons {} 数据！", list.size());
            return repository.saveAll(list).size();
        } else {
            log.info("总更新 Warframe.LichSisterWeapons {} 数据！", weaponsList.size());
            return repository.saveAll(weaponsList).size();
        }
    }

    //紫卡武器
    public static Integer getRivenWeapons() {
//        log.info("开始获取 Market Purple Weapon 数据！");
//        int attempts = 0;
//        while (attempts < 3) {
//            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_RIVEN_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
//            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
//                log.warn("市场紫卡武器初始化错误！未获得数据！尝试在 30 秒后再次获取它！");
//                sleepSeconds();
//                attempts++;
//                continue;
//            }
//            //获取数据源
//            List<RivenItems> items = JSON.parseObject(body.getBody())
//                    .getJSONObject("payload")
//                    .getJSONArray("items")
//                    .toJavaList(RivenItems.class, JSONReader.Feature.SupportSmartMatch);
//
//            RivenItemsRepository repository = SpringUtils.getBean(RivenItemsRepository.class);
//            AtomicInteger size = new AtomicInteger();
//            //判断数据库表中是否有数据
//            if (!repository.findAll().isEmpty()) {
//                List<RivenItems> all = repository.findAll();
//                //stream取差集，对比与数据库中不同的数据
//                List<RivenItems> list = items.stream().filter(item ->
//                                !all.stream()
//                                        //采用Map Key的方式对比多属性不同的值
//                                        .collect(Collectors.toMap(ri -> ri.getItemName() + "-" + ri.getIconFormat() + "-" + ri.getUrlName(), value -> value))
//                                        .containsKey(item.getItemName() + "-" + item.getIconFormat() + "-" + item.getUrlName())
//
//                        )
//                        //接受结果到List集合中
//                        .toList();
//                //便利结果集合
//                list.forEach(item -> {
//                    //到数据库中查询是否有这个值,如果有这个值则把ID付给要保存的新值
//                    repository.findById(item.getId()).ifPresent(b -> item.setRivenId(b.getRivenId()));
//                    //增加|修改值
//                    repository.save(item);
//                    //增加|修改值的数量
//                    size.addAndGet(1);
//                });
//                log.info("总计更新 Warframe.Market Riven LichSisterWeapons {} 数据！", size);
//                return size.get();
//            } else {
//                List<RivenItems> rivenItems = repository.saveAll(items);
//                log.info("总计更新 Warframe.Market Riven LichSisterWeapons {} 数据！", rivenItems.size());
//                return rivenItems.size();
//            }
//        }
//        log.warn("Market Purple Weapon 初始化错误！未获得数据！");
        return -1;

    }

    // 遗物
    public static Integer getRelics() {
//        log.info("开始初始化 Relics 数据！");
//        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_RELICS_DATA);
//        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
//            List<Relics> relics = JSON.parseObject(body.getBody()).getJSONArray("relics").toJavaList(Relics.class).stream().filter(r -> r.getState().equals("Intact")).toList();
//            relics = relics.stream().peek(r -> r.setRewards(r.getRewards().stream().peek(w -> w.setRelics(r)).toList())).toList();
//            RelicsRepository repository = SpringUtils.getBean(RelicsRepository.class);
//            if (!repository.findAll().isEmpty()) {
//                List<Relics> all = repository.findAll();
//                log.debug("Relics data is being filtered!");
//                List<Relics> list = relics.stream().filter(item ->
//                                !all.stream()
//                                        .collect(Collectors.toMap(re -> re.getRelicsId() + re.getRewards().stream().map(RelicsRewards::getRewardId).toList(), value -> value))
//                                        .containsKey(item.getRelicsId() + item.getRewards().stream().map(RelicsRewards::getRewardId).toList())
//                        )
//                        .toList();
//                relics = repository.saveAll(list);
//                log.info("总更新 Warframe.Relics {} 数据！", relics.size());
//            } else {
//                relics = repository.saveAll(relics);
//                log.info("总更新 Warframe.Relics {} 数据！", relics.size());
//            }
//            return relics.size();
//
//        }
        return -1;
    }

    //别名

    @SneakyThrows
    public static Integer getAlias() {
        log.info("开始初始化别名数据！");
        List<Alias> aliasList = JSON.parseArray(new File(DATA_SOURCE_PATH + "alias.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(Alias.class);
        AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);

        Map<String, Alias> allMap = createMap(aliasR.findAll(), Alias::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, Alias> uniqueTranslations = createMap(aliasList, Alias::getEquation, (oldVal, newVal) -> oldVal);

        List<Alias> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(Alias::new)
                .toList();

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

        List<RivenTion> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTion::new)
                .toList();

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

        List<RivenTionAlias> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTionAlias::new)
                .toList();

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

        List<Translation> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(Translation::new)
                .toList();
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

        List<RivenAnalyseTrend> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenAnalyseTrend::new)
                .toList();

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

        List<RivenTrend> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTrend::new)
                .toList();

        log.info("总计更新 Warframe.RivenTrend {} 数据！", r.saveAll(list).size());

        return list.size();
    }


    public static Boolean cloneDataSource() {
        log.info("开始初始化数据模板！");
        boolean flag = true;
        for (String url : ApiUrl.DATA_SOURCE_GIT) {
            try {
                log.debug("Clone data:{}", url);
                JgitUtil git = JgitUtil.Build(url, "");
                git.pull();
                flag = false;
                break;
            } catch (Exception e) {
                log.error("初始化数据模板不正确", e);
            }
        }
        return flag;
    }

    static void severExportFiles() {
        severExportFiles("zh", LAMA_PATH, INDEX_PATH, EXPORT_PATH);
    }

    /**
     * 获取DE官方数据文件并保存到本地
     *
     * @param languagesKey 语言
     * @param path         lzma 文件保存路径
     * @param outPath      解压文件保存路径
     * @param exportPath   索引数据保存路径 ./data/export/%s
     */
    private static void severExportFiles(String languagesKey, String path, String outPath, String exportPath) {
        Boolean files = getExportLZMAFiles(languagesKey, path);
        if (!files) {
            return;
        }
        if (ZipUtils.unLzma(path, outPath)) {
            List<String> keys = FileUtils.readFileToList(outPath);
            Map<String, String> compared = compareTheHashAndSave(keys);
            for (String key : compared.keySet()) {
                if (key.contains("ExportRecipes") || key.contains("ExportFusionBundles")) {
                    continue;
                }
                getExportFiles(key, exportPath.formatted(StringUtils.substring(key, 0, key.indexOf("!"))));
            }
        }
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
                    Arrays.stream(StateTypeEnum.values())
                            .filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY()))
                            .findFirst()
                            .ifPresentOrElse(s::setType, () -> s.setType(typeEnum));
                })
                .toList();
    }

    /**
     * 根据索引获取数据
     *
     * @param key  索引
     * @param path 文件保存路径
     */
    static Boolean getExportFiles(String key, String path) {
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

    <T> List<T> parsingExportJson(String exportPath, String key, Class<T> c) {
        JSONObject object;
        try {
            object = JSON.parseObject(new FileInputStream(exportPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return object.getList(key, c);
    }

    void initStateTranslation() {
        log.info("开始初始化 翻译 数据！");
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
        str.saveAll(stateTranslationList);
        try {
            List<StateTranslation> javaList = JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "state_translation.json"), JSONReader.Feature.SupportSmartMatch).toJavaList(StateTranslation.class)
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
        } catch (FileNotFoundException e) {
            log.error("state_translation.json文件不存在");
        }
        log.info("初始化状态翻译完成");
    }

    void initNodes() {
        List<Nodes> nodesList;
        nodesList = parsingExportJson(EXPORT_PATH.formatted("ExportRegions_zh.json"), "ExportRegions", Nodes.class);
        try {
            nodesList.addAll(JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "nodes.json")).toJavaList(Nodes.class));
        } catch (FileNotFoundException e) {
            log.error("nodes.json文件不存在");
        }
        nodesRepository.saveAll(nodesList);
    }

    void initRewardPool() {
        try {
            List<RewardPool> javaList = JSON.parseArray(new FileInputStream(DATA_SOURCE_PATH + "reward_pool.json")).toJavaList(RewardPool.class);
            rewardPoolRepository.saveAll(javaList);
        } catch (FileNotFoundException e) {
            log.error("reward_pool.json文件不存在");
        }
    }

    void initWeapons() {
        List<Weapons> weapons = parsingExportJson(EXPORT_PATH.formatted("ExportWeapons_zh.json"), "ExportWeapons", Weapons.class);
        weaponsRepository.saveAll(weapons);
    }

}
