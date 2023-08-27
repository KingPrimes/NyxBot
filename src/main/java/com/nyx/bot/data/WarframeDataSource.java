package com.nyx.bot.data;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.entity.warframe.Ephemeras;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.entity.warframe.Weapons;
import com.nyx.bot.repo.warframe.AliasRepository;
import com.nyx.bot.repo.warframe.EphemerasRepository;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
import com.nyx.bot.repo.warframe.WeaponsRepository;
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

    static String DataSourceUrl = "https://ghproxy.com/https://raw.githubusercontent.com/KingPrimes/DataSource/main/warframe/";

    public static void init() {
        log.info("开始插入Warframe数据！");
        getAlias();
        getMarket();
        getWeapons();
        getEphemeras();
    }

    private static void getEphemeras() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取幻纹信息！");
            Headers pairs = Headers.of("language", "zh-hans");
            String s = HttpUtils.sendGet("https://api.warframe.market/v1/lich/ephemeras", "", pairs);
            if (s.contains("timout") || s.contains("error")) {
                log.warn("赤毒幻纹信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            List<Ephemeras> ephemeras = JSONObject.parseObject(s.replaceAll("&", " ")).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class);

            s = HttpUtils.sendGet("https://api.warframe.market/v1/sister/ephemeras", "", pairs);
            if (s.contains("timout") || s.contains("error")) {
                log.warn("信条幻纹信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            ephemeras.addAll(JSONObject.parseObject(s.replaceAll("&", " ")).getJSONObject("payload").getJSONArray("ephemeras").toJavaList(Ephemeras.class));

            EphemerasRepository repository = SpringUtils.getBean(EphemerasRepository.class);

            if (repository.findAll().size() != ephemeras.size()) {
                AtomicInteger i = new AtomicInteger();
                ephemeras.forEach(e -> {
                    if(repository.findAll().isEmpty()){
                        i.addAndGet(repository.addEphemeras(e));
                    }else{
                        e.setId((long) (repository.queryMaxId() + 1));
                        i.addAndGet(repository.addEphemeras(e));
                    }
                });
                log.info("共更新Warframe.ephemeras {} 条数据！", i);
            }
        });
    }

    ;

    private static void getAlias() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取数据！");
            String s = HttpUtils.sendGet(DataSourceUrl + "warframe_alias.json");
            if (s.contains("timout") || s.contains("error")) {
                log.warn("别名表初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            JSONObject object = JSON.parseObject(s);
            List<Alias> records = object.getJSONArray("RECORDS").toJavaList(Alias.class);
            AliasRepository aliasR = SpringUtils.getBean(AliasRepository.class);
            AtomicInteger x = new AtomicInteger();
            if (aliasR.findAll().size() != records.size()) {
                records.forEach(a -> {
                    if(aliasR.findAll().isEmpty()){
                        x.addAndGet(aliasR.addAlias(a));
                    }else{
                        a.setId((long) (aliasR.queryMaxId() + 1));
                        x.addAndGet(aliasR.addAlias(a));
                    }
                });
                log.info("共更新Warframe别名表 {} 条数据！", x);
            }
        });
    }

    private static void getMarket() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取数据！");
            Headers pairs = Headers.of("language", "zh-hans");
            String s = HttpUtils.sendGet("https://api.warframe.market/v1/items", "", pairs);
            if (s.contains("timout") || s.contains("error")) {
                log.warn("Market初始化错误！未获取到数据信息！请检查网络！");
                return;
            }

            List<OrdersItems> items = JSON.parseObject(s.replaceAll("&", " ")).getJSONObject("payload").getJSONArray("items").toJavaList(OrdersItems.class, JSONReader.Feature.SupportSmartMatch);

            OrdersItemsRepository repository = SpringUtils.getBean(OrdersItemsRepository.class);
            if (repository.findAll().size() != items.size()) {
                AtomicInteger size = new AtomicInteger();
                items.forEach(i -> {
                    if(repository.findAll().isEmpty()){
                        size.addAndGet(repository.addOrdersItems(i));
                    }else{
                        i.setId((long) (repository.queryMaxId() + 1));
                        size.addAndGet(repository.addOrdersItems(i));
                    }
                });
                log.info("共更新Warframe.Market {} 条数据！", size);
            }
        });
    }

    private static void getWeapons() {
        AsyncUtils.me().execute(() -> {
            log.info("开始获取武器信息！");
            Headers pairs = Headers.of("language", "zh-hans");
            String s = HttpUtils.sendGet("https://api.warframe.market/v1/lich/weapons", "", pairs);
            if (s.contains("timout") || s.contains("error")) {
                log.warn("赤毒武器信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            List<Weapons> weapons = JSONObject.parseObject(s.replaceAll("&", " ")).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class);


            s = HttpUtils.sendGet("https://api.warframe.market/v1/sister/weapons", "", pairs);
            if (s.contains("timout") || s.contains("error")) {
                log.warn("信条武器信息初始化错误！未获取到数据信息！请检查网络！");
                return;
            }
            weapons.addAll(JSONObject.parseObject(s.replaceAll("&", " ")).getJSONObject("payload").getJSONArray("weapons").toJavaList(Weapons.class));

            WeaponsRepository repository = SpringUtils.getBean(WeaponsRepository.class);
            AtomicInteger i = new AtomicInteger();
            if (repository.findAll().size() != weapons.size()) {
                weapons.forEach(w -> {
                    if(repository.findAll().isEmpty()){
                        i.addAndGet(repository.addWeapons(w));
                    }else{
                        w.setId((long) (repository.queryMaxId() + 1));
                        i.addAndGet(repository.addWeapons(w));
                    }
                });
                log.info("共更新Warframe.Weapons {} 条数据！", i);
            }
        });
    }


}
