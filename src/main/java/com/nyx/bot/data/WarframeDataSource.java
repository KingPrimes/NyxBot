package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.*;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.warframe.*;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.HttpUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class WarframeDataSource {

    public static void init() {
        log.info("开始插入Warframe数据！");
        getAlias();
        getMarket();
        getWeapons();
        getEphemeras();
        initTranslation();
    }

    public static void getEphemeras() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取幻纹信息！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒幻纹信息初始化错误！未获取到数据信息！请检查网络！");
                return;
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
                log.info("共更新Warframe.ephemeras {} 条数据！", i);
                return;
            }
            log.info("ephemeras数据未变更！");
        });
    }

    public static void getAlias() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取数据！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_DATA_SOURCE + "alias.json");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("别名表初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            JSONObject object = JSON.parseObject(body.getBody(), JSONReader.Feature.SupportSmartMatch);
            List<Alias> records = object.getJSONArray("RECORDS").toJavaList(Alias.class);
            AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);
            // 更新
            if (aliasR.findAll().size() != records.size()) {
                records = aliasR.saveAll(records);
                log.info("共更新Warframe别名表 {} 条数据！", records.size());
                return;
            }
            log.info("Warframe别名表数据未变更！");
        });
    }

    public static void initTranslation() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取翻译数据！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_DATA_SOURCE + "translation.json");
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("翻译表初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            JSONObject object = JSON.parseObject(body.getBody(), JSONReader.Feature.SupportSmartMatch);
            List<Translation> records = object.getJSONArray("RECORDS").toJavaList(Translation.class);
            TranslationRepository t = SpringUtils.getBean(TranslationRepository.class);
            if (t.findAll().size() != records.size()) {
                records = t.saveAll(records);
                log.info("共更新Warframe翻译表 {} 条数据！", records.size());
                return;
            }
            log.info("Warframe翻译表数据未变更！");
        });
    }

    public static void getMarket() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取数据！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_ITEMS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("Market初始化错误！未获取到数据信息！请检查网络！");
                return;
            }

            List<OrdersItems> items = JSON.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("items").toJavaList(OrdersItems.class, JSONReader.Feature.SupportSmartMatch);

            OrdersItemsRepository repository = SpringUtils.getBean(OrdersItemsRepository.class);
            if (repository.findAll().size() != items.size()) {
                AtomicInteger size = new AtomicInteger();
                items.forEach(i -> {
                    if (repository.findAll().isEmpty()) {
                        size.addAndGet(repository.addOrdersItems(i));
                    } else {
                        i.setOid(repository.findTopByOrderByOidDesc().getOid() + 1);
                        size.addAndGet(repository.addOrdersItems(i));
                    }
                });
                log.info("共更新Warframe.Market {} 条数据！", size);
                return;
            }
            log.info("Warframe.Market数据未变更！");
        });
    }

    public static void getWeapons() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取武器信息！");
            HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("赤毒武器信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            List<Weapons> weapons = JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch);


            body = HttpUtils.sendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS, ApiUrl.LANGUAGE_ZH_HANS);
            if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
                log.warn("信条武器信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            weapons.addAll(JSONObject.parseObject(body.getBody()).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class, JSONReader.Feature.SupportSmartMatch));

            WeaponsRepository repository = SpringUtils.getBean(WeaponsRepository.class);
            AtomicInteger i = new AtomicInteger();
            if (repository.findAll().size() != weapons.size()) {
                weapons.forEach(w -> {
                    if (repository.findAll().isEmpty()) {
                        i.addAndGet(repository.addWeapons(w));
                    } else {
                        w.setId((long) (repository.queryMaxId() + 1));
                        i.addAndGet(repository.addWeapons(w));
                    }
                });
                log.info("共更新Warframe.Weapons {} 条数据！", i);
                return;
            }
            log.info("Warframe.Weapons数据未变更！");
        });
    }
}
