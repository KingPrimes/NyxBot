package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.*;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.*;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WarframeDataSource {

    public static void init() {
        log.info("开始初始化数据！");
        cloneDataSource();
        getAlias();
        getMarket();
        getWeapons();
        getEphemeras();
        initTranslation();
        getRivenAnalyseTrend();
        getRivenTrend();
        getRivenWeapons();
        getRivenTion();
        getRivenTionAlias();
    }

    //幻纹
    public static void getEphemeras() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取幻纹信息！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒幻纹信息初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getEphemeras();
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            String s = body.getBody();
            List<Ephemeras> ephemeras = JSONObject.parseObject(s).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch);

            body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("信条幻纹信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            ephemeras.addAll(JSONObject.parseObject(s).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class, JSONReader.Feature.SupportSmartMatch));

            EphemerasRepository repository = SpringUtils.getBean(EphemerasRepository.class);

            if (repository.findAll().size() != ephemeras.size()) {
                AtomicInteger i = new AtomicInteger();
                ephemeras.forEach(e -> {
                    if (repository.findAll().isEmpty()) {
                        i.addAndGet(repository.addEphemeras(e));
                    } else {
                        e.setEid((long) (repository.queryMaxId() + 1));
                        i.addAndGet(repository.addEphemeras(e));
                    }
                });
                log.info("共更新Warframe.Ephemeras {} 条数据！", i);
                return;
            }
            log.info("ephemeras数据未变更！");
        }, AsyncBeanName.InitData);
    }

    //别名
    public static void getAlias() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取别名数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "alias.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("别名表初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "alias.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {

                    }

                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<Alias> records = array.toJavaList(Alias.class);
                    AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);
                    if (!aliasR.findAll().isEmpty()) {
                        List<Alias> all = aliasR.findAll();
                        List<Alias> list = records.stream().filter(item ->
                                        !all.stream()
                                                .collect(Collectors.toMap(m -> m.getCn() + "-" + m.getEn(), value -> value))
                                                .containsKey(item.getCn() + "-" + item.getEn())
                                )
                                .toList();
                        records = aliasR.saveAll(list);
                        log.info("共更新Warframe别名表 {} 条数据！", records.size());
                    } else {
                        records = aliasR.saveAll(records);
                        log.info("共初始化Warframe别名表 {} 条数据！", records.size());
                    }
                    break;
                }
            }
        }, AsyncBeanName.InitData);
    }

    // 紫卡词条
    public static void getRivenTion() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取紫卡词条参数数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "market_riven_tion.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("紫卡词条参数表初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "market_riven_tion.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {

                    }

                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<RivenTion> rivenTionList = array.toJavaList(RivenTion.class);
                    RivenTionRepository records = SpringUtils.getBean(RivenTionRepository.class);
                    if (!records.findAll().isEmpty()) {
                        List<RivenTion> all = records.findAll();
                        List<RivenTion> list = rivenTionList.stream().filter(item ->
                                        !all.stream()
                                                .collect(Collectors.toMap(RivenTion::toString, value -> value))
                                                .containsKey(item.toString())
                                )
                                .toList();
                        rivenTionList = records.saveAll(list);
                        log.info("共更新Warframe紫卡词条参数表 {} 条数据！", rivenTionList.size());
                    } else {
                        rivenTionList = records.saveAll(rivenTionList);
                        log.info("共初始化Warframe紫卡词条参数表 {} 条数据！", rivenTionList.size());
                    }
                    break;
                }
            }
        }, AsyncBeanName.InitData);
    }

    // 紫卡词条别名
    public static void getRivenTionAlias() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取紫卡词条别名数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "market_riven_tion_alias.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("紫卡词条别名表初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "market_riven_tion_alias.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {

                    }
                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<RivenTionAlias> records = array.toJavaList(RivenTionAlias.class);
                    RivenTionAliasRepository aliasR = SpringUtils.getBean(RivenTionAliasRepository.class);
                    if (!aliasR.findAll().isEmpty()) {
                        List<RivenTionAlias> all = aliasR.findAll();
                        List<RivenTionAlias> list = records.stream().filter(item ->
                                        !all.stream()
                                                .collect(Collectors.toMap(RivenTionAlias::toString, value -> value))
                                                .containsKey(item.toString())
                                )
                                .toList();
                        log.info("紫卡词条别名表数据: {} ", list);
                        records = aliasR.saveAll(list);
                        log.info("共更新Warframe紫卡词条别名表 {} 条数据！", records.size());
                    } else {
                        records = aliasR.saveAll(records);
                        log.info("共初始化Warframe紫卡词条别名表 {} 条数据！", records.size());
                    }
                    break;
                }
            }
        }, AsyncBeanName.InitData);
    }


    //翻译
    public static void initTranslation() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取翻译数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "translation.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("翻译表初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "translation.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<Translation> translations = array.toJavaList(Translation.class);
                    TranslationRepository t = SpringUtils.getBean(TranslationRepository.class);
                    if (!t.findAll().isEmpty()) {
                        List<Translation> all = t.findAll();
                        List<Translation> list = translations.stream().filter(item ->
                                !all.stream()
                                        .collect(Collectors.toMap(m -> m.getCn() + "-" + m.getEn(), value -> value))
                                        .containsKey(item.getCn() + "-" + item.getEn())).toList();
                        translations = t.saveAll(list);
                        log.info("共更新Warframe翻译表 {} 条数据！", translations.size());
                    } else {
                        translations = t.saveAll(translations);
                        log.info("共初始化Warframe翻译表 {} 条数据！", translations.size());
                    }
                    break;
                }
            }

        }, AsyncBeanName.InitData);
    }

    //Market
    public static void getMarket() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取Market数据！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("Market初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getMarket();
                    return;
                } catch (InterruptedException ignored) {
                    return;
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
                list.forEach(item -> {
                    OrdersItems orders = ordersItem.findByOrderId(item.getOrderId());
                    Optional.ofNullable(orders).ifPresentOrElse(o -> {
                        item.setOid(o.getOid());
                        ordersItem.save(item);
                    }, () -> ordersItem.save(item));
                });
                log.info("共更新Warframe.Market {} 条数据！", list.size());
            } else {
                items = ordersItem.saveAll(items);
                log.info("共初始化Warframe.Market {} 条数据！", items.size());
            }
        }, AsyncBeanName.InitData);
    }

    //赤毒武器/信条武器
    public static void getWeapons() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取武器信息！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒武器信息初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getWeapons();
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            List<Weapons> weapons = JSONObject.parseObject(body.getBody())
                    .getJSONObject("payload")
                    .getJSONArray("weapons")
                    .toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch);


            body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("信条武器信息初始化错误！未获取到数据信息！请检查网络！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getWeapons();
                    return;
                } catch (InterruptedException ignored) {
                    return;
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
                list.forEach(item -> {
                    Weapons weaponsById = repository.findWeaponsById(item.getId());
                    Optional.ofNullable(weaponsById)
                            .ifPresentOrElse(weapon -> {
                                item.setWeaponId(weapon.getWeaponId());
                                repository.save(item);
                            }, () -> repository.save(item));
                });
                log.info("共更新Warframe.Weapons {} 条数据！", list.size());
            } else {
                log.info("共初始化Warframe.Weapons {} 条数据！", weapons.size());
            }
        }, AsyncBeanName.InitData);
    }

    //紫卡武器
    public static void getRivenWeapons() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取Market紫卡武器数据！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_Riven_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("Market紫卡武器初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getRivenWeapons();
                    return;
                } catch (InterruptedException ignored) {
                    return;
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
                    //到数据库中查询是否有这个值
                    RivenItems byRivenId = repository.findById(item.getId());
                    //如果有这个值则把ID付给要保存的新值
                    Optional.ofNullable(byRivenId).ifPresent(b ->
                            item.setRivenId(b.getRivenId()));
                    //增加|修改值
                    repository.save(item);
                    //增加|修改值的数量
                    size.addAndGet(1);
                });
                log.info("共更新Warframe.Market紫卡武器 {} 条数据！", size);
            } else {
                List<RivenItems> rivenItems = repository.saveAll(items);
                log.info("共更新Warframe.Market紫卡武器 {} 条数据！", rivenItems.size());
            }
        }, AsyncBeanName.InitData);
    }

    //紫卡计算器数据
    public static void getRivenAnalyseTrend() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取紫卡计算器数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "riven_analyse_trend.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("紫卡计算器数据初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "riven_analyse_trend.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<RivenAnalyseTrend> ratrs = array.toJavaList(RivenAnalyseTrend.class);
                    RivenAnalyseTrendRepository rater = SpringUtils.getBean(RivenAnalyseTrendRepository.class);
                    if (!rater.findAll().isEmpty()) {
                        List<RivenAnalyseTrend> all = rater.findAll();
                        List<RivenAnalyseTrend> list = ratrs.stream().filter(item ->
                                !all.stream().collect(Collectors.toMap(m ->
                                                        m.getArchwing() +
                                                                m.getName() +
                                                                m.getMelle() +
                                                                m.getPistol() +
                                                                m.getPrefix() +
                                                                m.getRifle() +
                                                                m.getShotgun() +
                                                                m.getSuffix(),
                                                value -> value))
                                        .containsKey(
                                                item.getArchwing() +
                                                        item.getName() +
                                                        item.getMelle() +
                                                        item.getPistol() +
                                                        item.getPrefix() +
                                                        item.getRifle() +
                                                        item.getShotgun() +
                                                        item.getSuffix()
                                        )).toList();
                        ratrs = rater.saveAll(list);
                        log.info("共更新紫卡计算器表 {} 条数据！", ratrs.size());
                    } else {
                        ratrs = rater.saveAll(ratrs);
                        log.info("共初始化紫卡计算器表 {} 条数据！", ratrs.size());
                    }
                    break;
                }
            }

        }, AsyncBeanName.InitData);
    }

    public static void getRivenTrend() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取紫卡倾向数据！");
            for (String url : ApiUrl.WARFRAME_DATA_SOURCE) {
                HttpUtils.Body body = HttpUtils.sendGet(url + "riven_trend.json");
                if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                    log.warn("紫卡倾向数据初始化错误！未获取到数据信息！30秒后尝试重新获取！URL：{}", url + "riven_trend.json");
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    JSONArray array = JSON.parseArray(body.getBody(), JSONReader.Feature.SupportSmartMatch);
                    List<RivenTrend> ratrs = array.toJavaList(RivenTrend.class);
                    RivenTrendRepository rater = SpringUtils.getBean(RivenTrendRepository.class);
                    if (!rater.findAll().isEmpty()) {
                        List<RivenTrend> all = rater.findAll();
                        List<RivenTrend> list = ratrs.stream().filter(item ->
                                !all.stream().collect(Collectors.toMap(m ->
                                                        m.getTrendName() +
                                                                m.getNewDot() +
                                                                m.getNewNum() +
                                                                m.getOldDot() +
                                                                m.getOldNum() +
                                                                m.getType(),
                                                value -> value))
                                        .containsKey(
                                                item.getTrendName() +
                                                        item.getNewDot() +
                                                        item.getNewNum() +
                                                        item.getOldDot() +
                                                        item.getOldNum() +
                                                        item.getType()
                                        )).toList();
                        ratrs = rater.saveAll(list);
                        log.info("共更新紫卡倾向表 {} 条数据！", ratrs.size());
                    } else {
                        ratrs = rater.saveAll(ratrs);
                        log.info("共初始化紫卡倾向表 {} 条数据！", ratrs.size());
                    }
                    break;
                }
            }

        }, AsyncBeanName.InitData);
    }

    public static void cloneDataSource() {
        AsyncUtils.me().execute(() -> {
            for (String url : ApiUrl.DATA_SOURCE_GIT) {
                try {
                    JgitUtil git = JgitUtil.Build(url, "");
                    TimeUnit.SECONDS.sleep(30);
                    git.pull();
                    break;
                } catch (Exception ignored) {
                    JgitUtil git = JgitUtil.Build(url, "");
                    try {
                        git.pull();
                        break;
                    } catch (Exception ignored1) {
                    }
                }
            }
        }, AsyncBeanName.InitData);
    }

}
