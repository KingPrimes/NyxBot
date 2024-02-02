package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.*;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.*;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.SpringUtils;
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
        log.info("开始插入Warframe数据！");
        getAlias(ApiUrl.WARFRAME_DATA_SOURCE_GIT_HUB);
        getMarket();
        getWeapons();
        getEphemeras();
        initTranslation(ApiUrl.WARFRAME_DATA_SOURCE_GIT_HUB);
        getRivenAnalyseTrend(ApiUrl.WARFRAME_DATA_SOURCE_GIT_HUB);
        getRivenWeapons();
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
        });
    }

    //别名
    public static void getAlias(String url) {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取别名数据！");
            HttpUtils.Body body = HttpUtils.sendGet(url + "alias.json");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("别名表初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getAlias(ApiUrl.WARFRAME_DATA_SOURCE_GIT_CODE);
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            JSONObject object = JSON.parseObject(body.getBody(), JSONReader.Feature.SupportSmartMatch);
            List<Alias> records = object.getJSONArray("RECORDS").toJavaList(Alias.class);
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
        });
    }

    //翻译
    public static void initTranslation(String url) {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取翻译数据！");
            HttpUtils.Body body = HttpUtils.sendGet(url + "translation.json");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("翻译表初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    initTranslation(ApiUrl.WARFRAME_DATA_SOURCE_GIT_CODE);
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            JSONObject object = JSON.parseObject(body.getBody(), JSONReader.Feature.SupportSmartMatch);
            List<Translation> translations = object.getJSONArray("RECORDS").toJavaList(Translation.class);
            TranslationRepository t = SpringUtils.getBean(TranslationRepository.class);
            if (!t.findAll().isEmpty()) {
                List<Translation> all = t.findAll();
                List<Translation> list = translations.stream().filter(item ->
                        !all.stream()
                                .collect(Collectors.toMap(m -> m.getCn() + "-" + m.getEn(), value -> value))
                                .containsKey(item.getCn() + "-" + item.getEn())).toList();
                translations = t.saveAll(translations);
                log.info("共更新Warframe翻译表 {} 条数据！", translations.size());
            } else {
                translations = t.saveAll(translations);
                log.info("共初始化Warframe翻译表 {} 条数据！", translations.size());
            }
        });
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
                    }, () -> {
                        ordersItem.save(item);
                    });
                });
                log.info("共更新Warframe.Market {} 条数据！", list.size());
            } else {
                items = ordersItem.saveAll(items);
                log.info("共初始化Warframe.Market {} 条数据！", items.size());
            }
        });
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
            List<Weapons> weapons = JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch);


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
                    Weapons weaponsById = repository.findWeaponsByWeaponId(item.getWeaponId());
                    Optional.ofNullable(weaponsById)
                            .ifPresentOrElse(weapon -> {
                                item.setId(weapon.getId());
                                repository.save(item);
                            }, () -> {
                                repository.save(item);
                            });
                });
                log.info("共更新Warframe.Weapons {} 条数据！", list.size());
            } else {
                log.info("共初始化Warframe.Weapons {} 条数据！", weapons.size());
            }
        });
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
            List<RivenItems> items = JSON.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("items").toJavaList(RivenItems.class, JSONReader.Feature.SupportSmartMatch);

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
                    RivenItems byRivenId = repository.findByRivenId(item.getRivenId());
                    //如果有这个值则把ID付给要保存的新值
                    Optional.ofNullable(byRivenId).ifPresent(b ->
                            item.setId(b.getId()));
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
        });
    }

    //紫卡计算器数据
    public static void getRivenAnalyseTrend(String url) {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取紫卡计算器数据！");
            HttpUtils.Body body = HttpUtils.sendGet(url + "riven_analyse_trend.json");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("紫卡计算器数据初始化错误！未获取到数据信息！30秒后尝试重新获取！");
                try {
                    TimeUnit.SECONDS.sleep(30);
                    getRivenAnalyseTrend(ApiUrl.WARFRAME_DATA_SOURCE_GIT_CODE);
                    return;
                } catch (InterruptedException ignored) {
                    return;
                }
            }
            JSONObject object = JSON.parseObject(body.getBody(), JSONReader.Feature.SupportSmartMatch);
            List<RivenAnalyseTrend> ratrs = object.getJSONArray("RECORDS").toJavaList(RivenAnalyseTrend.class);
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
        });
    }
}
