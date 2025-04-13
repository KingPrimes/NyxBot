package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.*;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.*;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WarframeDataSource {

    static final String DATA_SOURCE_PATH = JgitUtil.lockPath + "/warframe/";

    public static void init() {
        log.info("Start initializing the data！");
        // allOf等待所有任务完成
        CompletableFuture.allOf(
                // supplyAsync 异步获取数据,返回Boolean值用于下一个线程
                CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource)
                        // 根据上一个线程的返回值进行操作
                        .thenAccept(flag -> {
                            if (flag) {
                                log.error("Initialize data template, failed! Please check the network environment and restart the program!");
                                System.exit(SpringApplication.exit(SpringUtils.getApplicationContext(), () -> -1));
                            } else {
                                CompletableFuture.allOf(CompletableFuture.runAsync(WarframeDataSource::getAlias)
                                        .thenRunAsync(WarframeDataSource::getRivenTion)
                                        .thenRunAsync(WarframeDataSource::getRivenTionAlias)
                                        .thenRunAsync(WarframeDataSource::initTranslation)
                                        .thenRunAsync(WarframeDataSource::getRivenAnalyseTrend)
                                        .thenRunAsync(WarframeDataSource::getRivenTrend)).join();
                            }
                        }),
                // 初始化网络数据
                CompletableFuture.runAsync(WarframeDataSource::initWarframeStatus)
                        .thenRunAsync(WarframeDataSource::getEphemeras)
                        .thenRunAsync(WarframeDataSource::getMarket)
                        .thenRunAsync(WarframeDataSource::getWeapons)
                        .thenRunAsync(WarframeDataSource::getRivenWeapons)
                        .thenRunAsync(WarframeDataSource::getRelics)
        ).thenRun(() -> log.info("Data initialization complete!"));
    }

    public static void initWarframeStatus() {
        String a = FileUtils.readFileToString("./data/arbitration");
        String str = FileUtils.readFileToString("./data/status");
        if (!str.isEmpty()) {
            GlobalStates globalStates = JSON.parseObject(str, GlobalStates.class);
            CacheUtils.setGlobalState(globalStates);
        }
        if (!a.isEmpty()) {
            List<ArbitrationPre> arbitrationPres = JSON.parseArray(Base64.getDecoder().decode(a), ArbitrationPre.class);
            CacheUtils.setArbitration(arbitrationPres);
        }
    }

    //幻纹
    public static void getEphemeras() {
        log.info("Start getting the Phantom Pattern information!");
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("The information initialization of the kuss phantom pattern is incorrect! No data obtained! Try to get it again after 30 seconds!");
            try {
                TimeUnit.SECONDS.sleep(30);
                getEphemeras();
                return;
            } catch (InterruptedException ignored) {
                getEphemeras();
                return;
            }
        }
        String s = body.getBody();
        List<Ephemeras> ephemerasList = JSONObject.parseObject(s).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch);
        body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("Creed Phantom Message Initialization Error! No data obtained! Please check the network!");
            return;
        }
        s = body.getBody();
        ephemerasList.addAll(JSONObject.parseObject(s).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch));
        EphemerasRepository repository = SpringUtils.getBean(EphemerasRepository.class);
        if (!repository.findAll().isEmpty()) {
            List<Ephemeras> all = repository.findAll();
            List<Ephemeras> list = ephemerasList.stream()
                    .filter(i -> !all.stream()
                            .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName() + m.getElement(), value -> value))
                            .containsKey(i.getItemName() + i.getUrlName() + i.getElement())
                    ).toList();
            repository.saveAll(list);
            log.info("Total updates Warframe.Ephemeras {} data!", list.size());
        } else {
            repository.saveAll(ephemerasList);
            log.info("Total updates Warframe.Ephemeras {} data！", ephemerasList.size());
        }
    }

    //Market
    public static Integer getMarket() {
        log.info("Get started with Market data!");
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("Market Initialization error! No data obtained! Try to get it again after 30 seconds!");
            try {
                TimeUnit.SECONDS.sleep(30);
                getMarket();
                return -1;
            } catch (InterruptedException ignored) {
                getMarket();
                return -1;
            }
        }

        List<OrdersItems> items = JSON.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("items").toJavaList(OrdersItems.class, JSONReader.Feature.SupportSmartMatch);
        OrdersItemsRepository ordersItem = SpringUtils.getBean(OrdersItemsRepository.class);
        if (!ordersItem.findAll().isEmpty()) {
            List<OrdersItems> all = ordersItem.findAll();
            List<OrdersItems> list = items.stream()
                    .filter(item -> !all.stream()
                            .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName(), value -> value))
                            .containsKey(item.getItemName() + item.getUrlName())).toList();
            log.info("Total updates Warframe.Market {} data！", list.size());
            return ordersItem.saveAll(list).size();
        } else {
            log.info("Total updates Warframe.Market {} data！", items.size());
            return ordersItem.saveAll(items).size();

        }
    }

    //赤毒武器/信条武器
    public static Integer getWeapons() {
        log.info("Start getting weapon information!");
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("Kuva weapon information initialization error! No data obtained! Try to get it again after 30 seconds!");
            try {
                TimeUnit.SECONDS.sleep(30);
                getWeapons();
                return -1;
            } catch (InterruptedException ignored) {
                getWeapons();
                return -1;
            }
        }
        List<Weapons> weapons = JSONObject.parseObject(body.getBody())
                .getJSONObject("payload")
                .getJSONArray("weapons")
                .toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch);


        body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("Creed weapon info initialization error! No data obtained! Please check the network!");
            try {
                TimeUnit.SECONDS.sleep(30);
                getWeapons();
                return -1;
            } catch (InterruptedException ignored) {
                return -1;
            }
        }
        weapons.addAll(JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch));

        WeaponsRepository repository = SpringUtils.getBean(WeaponsRepository.class);
        if (!repository.findAll().isEmpty()) {
            List<Weapons> all = repository.findAll();
            List<Weapons> list = weapons.stream()
                    .filter(item -> !all.stream()
                            .collect(Collectors.toMap(m -> m.getItemName() + m.getUrlName(), value -> value))
                            .containsKey(item.getItemName() + item.getUrlName())).toList();
            log.info("Total updates Warframe.Weapons {} data！", list.size());
            return repository.saveAll(list).size();
        } else {
            log.info("Total updates Warframe.Weapons {} data！", weapons.size());
            return repository.saveAll(weapons).size();
        }
    }

    //紫卡武器
    public static Integer getRivenWeapons() {
        log.info("Start getting Market Purple Weapon Data!");
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_RIVEN_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            log.warn("Market Purple Card Weapon Initialization Error! No data obtained! Try to get it again after 30 seconds!");
            try {
                TimeUnit.SECONDS.sleep(30);
                getRivenWeapons();
                return -1;
            } catch (InterruptedException ignored) {
                getRivenWeapons();
                return -1;
            }
        }
        //获取数据源
        List<RivenItems> items = JSON.parseObject(body.getBody())
                .getJSONObject("payload")
                .getJSONArray("items")
                .toJavaList(RivenItems.class, JSONReader.Feature.SupportSmartMatch);

        RivenItemsRepository repository = SpringUtils.getBean(RivenItemsRepository.class);
        AtomicInteger size = new AtomicInteger();
        //判断数据库表中是否有数据
        if (!repository.findAll().isEmpty()) {
            List<RivenItems> all = repository.findAll();
            //stream取差集，对比与数据库中不同的数据
            List<RivenItems> list = items.stream().filter(item ->
                            !all.stream()
                                    //采用Map Key的方式对比多属性不同的值
                                    .collect(Collectors.toMap(ri -> ri.getItemName() + "-" + ri.getIconFormat() + "-" + ri.getUrlName(), value -> value))
                                    .containsKey(item.getItemName() + "-" + item.getIconFormat() + "-" + item.getUrlName())

                    )
                    //接受结果到List集合中
                    .toList();
            //便利结果集合
            list.forEach(item -> {
                //到数据库中查询是否有这个值,如果有这个值则把ID付给要保存的新值
                repository.findById(item.getId()).ifPresent(b -> item.setRivenId(b.getRivenId()));
                //增加|修改值
                repository.save(item);
                //增加|修改值的数量
                size.addAndGet(1);
            });
            log.info("Total updates Warframe.Market Riven Weapons {} data！", size);
            return size.get();
        } else {
            List<RivenItems> rivenItems = repository.saveAll(items);
            log.info("Total updates Warframe.Market Riven Weapons {} data！", rivenItems.size());
            return rivenItems.size();
        }
    }

    // 遗物
    public static Integer getRelics() {
        log.info("Start initializing Relics data!");
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_RELICS_DATA);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            List<Relics> relics = JSON.parseObject(body.getBody()).getJSONArray("relics").toJavaList(Relics.class).stream().filter(r -> r.getState().equals("Intact")).toList();
            relics = relics.stream().peek(r -> r.setRewards(r.getRewards().stream().peek(w -> w.setRelics(r)).toList())).toList();
            RelicsRepository repository = SpringUtils.getBean(RelicsRepository.class);
            if (!repository.findAll().isEmpty()) {
                List<Relics> all = repository.findAll();
                log.debug("Relics data is being filtered!");
                List<Relics> list = relics.stream().filter(item ->
                                !all.stream()
                                        .collect(Collectors.toMap(re -> re.getRelicsId() + re.getRewards().stream().map(RelicsRewards::getRewardId).toList(), value -> value))
                                        .containsKey(item.getRelicsId() + item.getRewards().stream().map(RelicsRewards::getRewardId).toList())
                        )
                        .toList();
                relics = repository.saveAll(list);
                log.info("Total updates Warframe.Relics {} data！", relics.size());
            } else {
                relics = repository.saveAll(relics);
                log.info("Total updates Warframe.Relics {} data！", relics.size());
            }
            return relics.size();

        }
        return -1;
    }

    //别名

    @SneakyThrows
    public static Integer getAlias() {
        log.info("Start initializing alias data!");
        List<Alias> aliasList = JSON.parseArray(new File(DATA_SOURCE_PATH + "alias.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(Alias.class);
        AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);

        Map<String, Alias> allMap = createMap(aliasR.findAll(), Alias::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, Alias> uniqueTranslations = createMap(aliasList, Alias::getEquation, (oldVal, newVal) -> oldVal);

        List<Alias> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(Alias::new)
                .toList();

        log.info("Total updates Warframe.Alias {} data！", aliasR.saveAll(list).size());

        return list.size();
    }

    // 紫卡词条
    @SneakyThrows
    public static Integer getRivenTion() {
        log.info("Start initializing the Purple Card entry parameter data!");
        List<RivenTion> rivenTions = JSON.parseArray(new File(DATA_SOURCE_PATH + "market_riven_tion.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTion.class);
        RivenTionRepository records = SpringUtils.getBean(RivenTionRepository.class);

        Map<String, RivenTion> allMap = createMap(records.findAll(), RivenTion::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTion> uniqueTranslations = createMap(rivenTions, RivenTion::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTion> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTion::new)
                .toList();

        log.info("Total updates Warframe.RivenTion {} data！", records.saveAll(list).size());
        return list.size();
    }

    // 紫卡词条别名
    @SneakyThrows
    public static Integer getRivenTionAlias() {
        log.info("Start initializing the Purple Card entry alias data!");
        List<RivenTionAlias> rivenTionAliases = JSON.parseArray(new File(DATA_SOURCE_PATH + "market_riven_tion_alias.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTionAlias.class);
        RivenTionAliasRepository repository = SpringUtils.getBean(RivenTionAliasRepository.class);

        Map<String, RivenTionAlias> allMap = createMap(repository.findAll(), RivenTionAlias::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTionAlias> uniqueTranslations = createMap(rivenTionAliases, RivenTionAlias::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTionAlias> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTionAlias::new)
                .toList();

        log.info("Total updates Warframe.RivenTion.Alias {} data！", repository.saveAll(list).size());
        return list.size();
    }

    //翻译
    @SneakyThrows
    public static Integer initTranslation() {
        log.info("Start initializing your translation data!");
        List<Translation> translations = JSON.parseArray(new File(DATA_SOURCE_PATH + "translation.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(Translation.class);
        TranslationRepository t = SpringUtils.getBean(TranslationRepository.class);

        Map<String, Translation> allMap = createMap(t.findAll(), Translation::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, Translation> uniqueTranslations = createMap(translations, Translation::getEquation, (oldVal, newVal) -> oldVal);

        List<Translation> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(Translation::new)
                .toList();
        log.info("Total updates Warframe.Translation {} data！", t.saveAll(list).size());
        return list.size();
    }

    //紫卡计算器数据
    @SneakyThrows
    public static Integer getRivenAnalyseTrend() {
        log.info("Start initializing RivenAnalyseTrend data!");
        List<RivenAnalyseTrend> translations = JSON.parseArray(new File(DATA_SOURCE_PATH + "riven_analyse_trend.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenAnalyseTrend.class);
        RivenAnalyseTrendRepository r = SpringUtils.getBean(RivenAnalyseTrendRepository.class);

        Map<String, RivenAnalyseTrend> allMap = createMap(r.findAll(), RivenAnalyseTrend::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenAnalyseTrend> uniqueTranslations = createMap(translations, RivenAnalyseTrend::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenAnalyseTrend> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenAnalyseTrend::new)
                .toList();

        log.info("Total updates Warframe.RivenAnalyseTrend {} data！", r.saveAll(list).size());

        return list.size();
    }

    @SneakyThrows
    public static Integer getRivenTrend() {
        log.info("Start initializing RivenTrend data!");
        List<RivenTrend> rt = JSON.parseArray(new File(DATA_SOURCE_PATH + "riven_trend.json").toURI().toURL(), JSONReader.Feature.SupportSmartMatch).toJavaList(RivenTrend.class);
        RivenTrendRepository r = SpringUtils.getBean(RivenTrendRepository.class);

        Map<String, RivenTrend> allMap = createMap(r.findAll(), RivenTrend::getEquation, (oldVal, newVal) -> oldVal);

        Map<String, RivenTrend> uniqueTranslations = createMap(rt, RivenTrend::getEquation, (oldVal, newVal) -> oldVal);

        List<RivenTrend> list = uniqueTranslations.values().stream()
                .filter(item -> !allMap.containsKey(item.getEquation()))
                .map(RivenTrend::new)
                .toList();

        log.info("Total updates Warframe.RivenTrend {} data！", r.saveAll(list).size());

        return list.size();
    }

    public static Boolean cloneDataSource() {
        log.info("Start initializing data template!");
        boolean flag = true;
        for (String url : ApiUrl.DATA_SOURCE_GIT) {
            try {
                log.debug("Clone data:{}", url);
                JgitUtil git = JgitUtil.Build(url, "");
                git.pull();
                flag = false;
                break;
            } catch (Exception e) {
                log.warn("The initialization data template is incorrect", e);
            }
        }
        return flag;
    }

    private static <T, K> Map<K, T> createMap(Collection<T> items, Function<T, K> keyMapper, BinaryOperator<T> mergeFunction) {
        return items.stream().collect(Collectors.toMap(keyMapper, Function.identity(), mergeFunction));
    }

}
