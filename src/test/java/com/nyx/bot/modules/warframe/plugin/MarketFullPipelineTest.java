package com.nyx.bot.modules.warframe.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.SystemInfoUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.defaultdraw.DefaultDrawImagePlugin;
import io.github.kingprimes.model.Ducats;
import io.github.kingprimes.model.Relics;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import io.github.kingprimes.model.enums.MarketStatusEnum;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.RarityEnum;
import io.github.kingprimes.model.market.MarketLichSister;
import io.github.kingprimes.model.market.MarketRiven;
import io.github.kingprimes.model.market.OrderWithUser;
import io.github.kingprimes.model.market.Orders;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Market / 帮助 / 状态 / 订阅 完整流程集成测试。
 * <p>链路：API/H2数据 → 处理/转换 → DefaultDrawImagePlugin 绘图 → temp/test-images-market/</p>
 */
@DisplayName("Market 完整流程集成测试")
class MarketFullPipelineTest {

    static final Path OUT = Path.of("temp", "test-images-market");
    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    static final String MARKET_DUCATS_URL = "https://api.warframe.market/v1/tools/ducats";
    static final String MARKET_AUCTION_SEARCH = "https://api.warframe.market/v1/auctions/search";

    // H2 JDBC 连接
    static final String DB_URL = "jdbc:h2:file:./data/H2;MODE=MySQL;AUTO_SERVER=TRUE;PAGE_SIZE=16";
    static final String DB_USER = "sa", DB_PASS = "sa";

    private static DefaultDrawImagePlugin drawImagePlugin;
    private static Ducats ducats;
    private static List<Relics> relicsList;
    private static String rivenSlug;
    private static String lichSlug;
    private static String sisterSlug;
    private static String orderSlug;
    private static Map<String, OrdersItemsInfo> ordersItemInfoMap;
    private static Map<String, String> rivenTionMap; // url_name → effect 中文翻译

    @BeforeAll
    static void beforeAll() throws Exception {
        drawImagePlugin = new DefaultDrawImagePlugin();
        Files.createDirectories(OUT);

        // 从 Market API 获取杜卡币数据
        var body = HttpUtils.sendGet(MARKET_DUCATS_URL);
        System.out.println("[API] Ducats HTTP " + body.code() + " body:" + body.body().length() + " chars");
        ducats = MAPPER.readValue(body.body(), Ducats.class);
        System.out.println("[API] Ducats day=" + (ducats.getPayload().getPreviousDay() != null ? ducats.getPayload().getPreviousDay().size() : 0)
                + " hour=" + (ducats.getPayload().getPreviousHour() != null ? ducats.getPayload().getPreviousHour().size() : 0));

        // 从 H2 加载遗物数据
        relicsList = loadRelics();
        System.out.println("[H2] relics: " + relicsList.size() + " 条");

        // 从 H2 加载 market 查询所需的 slug
        rivenSlug = loadFirstSlug("riven_items", "slug", "");
        // lich/sister 使用同一个表，按 slug 名称区分
        lichSlug = loadFirstSlug("lich_sister_weapons", "slug", "AND slug LIKE 'kuva%'");
        sisterSlug = loadFirstSlug("lich_sister_weapons", "slug", "AND slug LIKE 'tenet%'");
        if (sisterSlug == null) {
            sisterSlug = loadFirstSlug("lich_sister_weapons", "slug", "AND slug NOT LIKE 'kuva%'");
        }
        orderSlug = loadFirstSlug("orders_items", "slug", "AND slug IS NOT NULL AND slug != ''");
        ordersItemInfoMap = loadOrdersItemInfo();
        rivenTionMap = loadRivenTionMap();
        System.out.println("[H2] rivenTionMap: " + rivenTionMap.size() + " 条");
        System.out.println("[H2] rivenSlug=" + rivenSlug + " lichSlug=" + lichSlug
                + " sisterSlug=" + sisterSlug + " orderSlug=" + orderSlug);
    }

    @AfterAll
    static void afterAll() {
        // no-op
    }

    // ======================== H2 数据加载 ========================

    private static String loadFirstSlug(String table, String column) throws Exception {
        return loadFirstSlug(table, column, "");
    }

    private static String loadFirstSlug(String table, String column, String extraWhere) throws Exception {
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT " + column + " FROM " + table + " WHERE " + column + " IS NOT NULL " + extraWhere + " LIMIT 1")) {
            return rs.next() ? rs.getString(column) : null;
        }
    }

    private static Map<String, String> loadRivenTionMap() throws Exception {
        Map<String, String> map = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT url_name, effect FROM riven_tion")) {
            while (rs.next()) {
                map.put(rs.getString("url_name"), rs.getString("effect"));
            }
        }
        return map;
    }

    /**
     * 模拟 {@code MarketRivenUtils.mapAttributeEffects()}：将 urlName 替换为中文翻译。
     */
    private static void translateRivenAttributes(MarketRiven marketRiven) {
        if (marketRiven.getPayload() == null || marketRiven.getPayload().getAuctions() == null) return;
        for (var auction : marketRiven.getPayload().getAuctions()) {
            if (auction.getItem() == null || auction.getItem().getAttributes() == null) continue;
            for (var attr : auction.getItem().getAttributes()) {
                String translated = rivenTionMap.get(attr.getUrlName());
                if (translated != null) {
                    attr.setUrlName(translated);
                }
            }
        }
    }

    private static List<Relics> loadRelics() throws Exception {
        Map<String, List<Relics.Rewards>> relicRewardsMap = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT r.name as relic_name, rr.reward_name, rr.rarity, rr.item_count " +
                             "FROM relics r JOIN relic_rewards rr ON r.unique_name = rr.relics_id")) {
            while (rs.next()) {
                String relicName = rs.getString("relic_name");
                Relics.Rewards reward = new Relics.Rewards();
                reward.setName(rs.getString("reward_name"));
                reward.setRarity(RarityEnum.values()[rs.getInt("rarity")]);
                reward.setItemCount(rs.getInt("item_count"));
                relicRewardsMap.computeIfAbsent(relicName, k -> new ArrayList<>()).add(reward);
            }
        }

        List<Relics> result = new ArrayList<>();
        for (var entry : relicRewardsMap.entrySet()) {
            Relics r = new Relics();
            r.setName(entry.getKey());
            r.setRewards(entry.getValue());
            result.add(r);
        }
        result.sort(Comparator.comparing(Relics::getName));
        return result;
    }

    private static Map<String, OrdersItemsInfo> loadOrdersItemInfo() throws Exception {
        Map<String, OrdersItemsInfo> map = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT slug, name, ducats, vaulted, max_amber_stars, max_cyan_stars, base_endo " +
                             "FROM orders_items WHERE slug IS NOT NULL AND slug != ''")) {
            while (rs.next()) {
                OrdersItemsInfo info = new OrdersItemsInfo();
                info.slug = rs.getString("slug");
                info.name = rs.getString("name");
                info.ducats = rs.getInt("ducats");
                info.vaulted = rs.getBoolean("vaulted");
                info.maxAmberStars = rs.getInt("max_amber_stars");
                info.maxCyanStars = rs.getInt("max_cyan_stars");
                info.baseEndo = rs.getInt("base_endo");
                map.put(info.slug, info);
            }
        }
        System.out.println("[H2] orders_items: " + map.size() + " 条");
        return map;
    }

    private static Map<Ducats.DumpType, List<Ducats.Ducat>> buildGodDump() {
        Map<Ducats.DumpType, List<Ducats.Ducat>> map = new LinkedHashMap<>();
        if (ducats.getPayload().getPreviousDay() != null) {
            map.put(Ducats.DumpType.DAY, filterAndSortDucats(ducats.getPayload().getPreviousDay(), true));
        }
        if (ducats.getPayload().getPreviousHour() != null) {
            map.put(Ducats.DumpType.HOUR, filterAndSortDucats(ducats.getPayload().getPreviousHour(), true));
        }
        return map;
    }

    // ======================== Ducats 辅助方法 ========================

    private static Map<Ducats.DumpType, List<Ducats.Ducat>> buildSilverDump() {
        Map<Ducats.DumpType, List<Ducats.Ducat>> map = new LinkedHashMap<>();
        if (ducats.getPayload().getPreviousDay() != null) {
            map.put(Ducats.DumpType.DAY, filterAndSortDucats(ducats.getPayload().getPreviousDay(), false));
        }
        if (ducats.getPayload().getPreviousHour() != null) {
            map.put(Ducats.DumpType.HOUR, filterAndSortDucats(ducats.getPayload().getPreviousHour(), false));
        }
        return map;
    }

    private static List<Ducats.Ducat> filterAndSortDucats(List<Ducats.Ducat> list, boolean god) {
        return list.stream()
                .filter(d -> god ? d.getDucats() == 100 : (d.getDucats() >= 45 && d.getDucats() < 100))
                .sorted(Comparator.comparingDouble((Ducats.Ducat d) ->
                        d.getDucatsPerPlatinumWa() != null ? d.getDucatsPerPlatinumWa() : 0).reversed())
                .limit(10)
                .toList();
    }

    private static Map<Integer, String> buildSubscribeMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
//        for (SubscribeEnums e : SubscribeEnums.values()) {
//            if (e == SubscribeEnums.ERROR) continue;
//            map.put(e.ordinal(), e.getNAME());
//        }
        return map;
    }

    // ======================== Subscribe 数据构建 ========================

    private static Map<Integer, String> buildMissionTypeMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (MissionTypeEnum e : MissionTypeEnum.getOrderedValues()) {
            map.put(e.getOrder(), e.getName());
        }
        return map;
    }

    private static Map<Integer, String> buildinvasionRewardMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (InvasionReward e : InvasionReward.values()) {
            map.put(e.ordinal(), e.getName());
        }
        return map;
    }

    /**
     * 模拟 {@code MarketRivenUtils.stream()} / {@code MarketLichSisterUtils.processAuctionData()} 的过滤逻辑：
     * 移除已关闭、不可见、离线用户的拍卖，按价格排序，限条数。
     */
    private static <T> List<T> filterAuctions(List<T> auctions,
                                              Function<T, Boolean> closedFn,
                                              Function<T, Boolean> visibleFn,
                                              Function<T, String> statusFn,
                                              Function<T, Integer> buyoutFn,
                                              Function<T, Integer> startingFn,
                                              Function<T, Integer> topBidFn) {
        return auctions.stream()
                .filter(a -> closedFn.apply(a) == null || !closedFn.apply(a))
                .filter(a -> visibleFn.apply(a) == null || visibleFn.apply(a))
                .filter(a -> {
                    String status = statusFn.apply(a);
                    return status != null && (status.equalsIgnoreCase("ingame") || status.equalsIgnoreCase("online"));
                })
                .sorted(Comparator
                        .comparingInt((T a) -> buyoutFn.apply(a) != null ? buyoutFn.apply(a) : Integer.MAX_VALUE)
                        .thenComparingInt(a -> startingFn.apply(a) != null ? startingFn.apply(a) : Integer.MAX_VALUE)
                        .thenComparingInt(a -> topBidFn.apply(a) != null ? topBidFn.apply(a) : Integer.MAX_VALUE))
                .limit(10)
                .toList();
    }

    // ======================== Auction 过滤辅助 ========================

    private static List<String> buildHelpCommands() {
        return Arrays.stream(Codes.values())
                .map(c -> StringUtils.removeMatcher(c.getComm()))
                .map(s -> {
                    if (s.startsWith("取消订阅")) return "取消订阅 [0-9] -[0-9]";
                    if (s.startsWith("订阅")) return "订阅 [0-9] -[0-9]";
                    return s;
                })
                .distinct()
                .toList();
    }

    // ======================== Help 数据构建 ========================

    private static String fetchAuctionJson(String type, String slug) throws Exception {
        String url = MARKET_AUCTION_SEARCH + "?type=" + type + "&weapon_url_name=" + slug + "&sort_by=price_asc";
        var body = HttpUtils.sendGet(url);
        String bodyStr = body.body();
        System.out.println("  [API] " + type + " HTTP " + body.code() + " body:" + (bodyStr != null ? bodyStr.length() : 0) + " chars");
        if (!body.is2xxSuccessful() || bodyStr == null) {
            throw new RuntimeException(type + " API 请求失败: " + body.code() + " slug=" + slug);
        }
        return bodyStr;
    }

    // ======================== Market API 辅助方法 ========================

    private static class OrdersItemsInfo {
        String slug, name;
        int ducats, maxAmberStars, maxCyanStars, baseEndo;
        boolean vaulted;
    }

    // ======================== 测试 ========================

    @Nested
    @DisplayName("1. 帮助 — Codes 枚举 → drawHelpImage")
    class Help_ {
        @Test
        void test() throws Exception {
            List<String> commands = buildHelpCommands();
            assertFalse(commands.isEmpty(), "帮助命令列表不应为空");
            byte[] img = drawImagePlugin.drawHelpImage(commands);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("help.png"), img);
            System.out.println("[help] OK " + img.length + "B " + commands.size() + "条命令");
        }
    }

    @Nested
    @DisplayName("2. 系统状态 — SystemInfoUtils → drawAllInfoImage")
    class SystemInfo_ {
        @Test
        void test() throws Exception {
            var allInfo = SystemInfoUtils.getInfo();
            assertNotNull(allInfo);
            assertNotNull(allInfo.getCpuInfo(), "CPU信息不应为空");
            assertNotNull(allInfo.getJvmInfo(), "JVM信息不应为空");
            byte[] img = drawImagePlugin.drawAllInfoImage(allInfo);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("system_info.png"), img);
            System.out.println("[system_info] OK " + img.length + "B cpu=" + allInfo.getCpuInfo().getModel());
        }
    }

    @Nested
    @DisplayName("3. 订阅说明 — SubscribeEnums+MissionTypeEnum → drawWarframeSubscribeImage")
    class Subscribe_ {
        @Test
        void test() throws Exception {
            var subscribeMap = buildSubscribeMap();
            var missionTypeMap = buildMissionTypeMap();
            var integerStringMap = buildinvasionRewardMap();
            assertFalse(subscribeMap.isEmpty());
            assertFalse(missionTypeMap.isEmpty());
            byte[] img = drawImagePlugin.drawWarframeSubscribeImage(subscribeMap, missionTypeMap,integerStringMap);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("subscribe.png"), img);
            System.out.println("[subscribe] OK " + img.length + "B subscribe=" + subscribeMap.size() + " missionType=" + missionTypeMap.size());
        }
    }

    @Nested
    @DisplayName("4. 市场金垃圾 — Ducats API → drawMarketGodDumpImage")
    class MarketGodDump_ {
        @Test
        void test() throws Exception {
            assertNotNull(ducats, "杜卡币API应返回数据");
            var dump = buildGodDump();
            assertFalse(dump.isEmpty(), "金垃圾数据不应为空");
            dump.forEach((type, list) ->
                    System.out.println("  [god] " + type + ": " + list.size() + "条"));
            byte[] img = drawImagePlugin.drawMarketGodDumpImage(dump);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_god_dump.png"), img);
            System.out.println("[market_god_dump] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("5. 市场银垃圾 — Ducats API → drawMarketSilverDumpImage")
    class MarketSilverDump_ {
        @Test
        void test() throws Exception {
            assertNotNull(ducats, "杜卡币API应返回数据");
            var dump = buildSilverDump();
            assertFalse(dump.isEmpty(), "银垃圾数据不应为空");
            dump.forEach((type, list) ->
                    System.out.println("  [silver] " + type + ": " + list.size() + "条"));
            byte[] img = drawImagePlugin.drawMarketSilverDumpImage(dump);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_silver_dump.png"), img);
            System.out.println("[market_silver_dump] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("6. 遗物 — H2数据库 → drawRelicsImage")
    class Relics_ {
        @Test
        void test() throws Exception {
            assertFalse(relicsList.isEmpty(), "遗物数据不应为空");
            var subset = relicsList.subList(0, Math.min(5, relicsList.size()));
            byte[] img = drawImagePlugin.drawRelicsImage(subset);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("relics.png"), img);
            System.out.println("[relics] OK " + img.length + "B total=" + relicsList.size() + " drawn=" + subset.size());
        }
    }

    @Nested
    @DisplayName("7. 市场紫卡 — API → 过滤(closed/visible/online) → drawMarketRivenImage")
    class MarketRiven_ {
        @Test
        void test() throws Exception {
            assertNotNull(rivenSlug, "应从H2获取紫卡武器slug");
            String json = fetchAuctionJson("riven", rivenSlug);
            MarketRiven marketRiven = MAPPER.readValue(json, MarketRiven.class);
            assertNotNull(marketRiven);
            assertNotNull(marketRiven.getPayload(), "payload不应为空");
            var raw = marketRiven.getPayload().getAuctions();
            assertFalse(raw.isEmpty(), "拍卖数据不应为空");
            System.out.println("  [riven] raw=" + raw.size() + " item=" + marketRiven.getItemName());

            // 模拟 MarketRivenUtils.stream() 过滤
            var filtered = filterAuctions(raw,
                    MarketRiven.Auctions::getClosed,
                    MarketRiven.Auctions::getVisible,
                    a -> a.getOwner() != null ? a.getOwner().getStatus() : null,
                    MarketRiven.Auctions::getBuyoutPrice,
                    MarketRiven.Auctions::getStartingPrice,
                    MarketRiven.Auctions::getTopBid
            );
            assertFalse(filtered.isEmpty(), "过滤后应有在线用户拍卖");
            marketRiven.getPayload().setAuctions(filtered);
            marketRiven.setItemName(rivenSlug);
            // 模拟 MarketRivenUtils.mapAttributeEffects() 翻译词条
            translateRivenAttributes(marketRiven);
            System.out.println("  [riven] filtered=" + filtered.size());

            byte[] img = drawImagePlugin.drawMarketRivenImage(marketRiven);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_riven.png"), img);
            System.out.println("[market_riven] OK " + img.length + "B slug=" + rivenSlug);
        }
    }

    @Nested
    @DisplayName("8. 市场赤毒 — API → 过滤(closed/visible/online) → drawMarketLichesImage")
    class MarketLiches_ {
        @Test
        void test() throws Exception {
            assertNotNull(lichSlug, "应从H2获取赤毒武器slug");
            String json = fetchAuctionJson("lich", lichSlug);
            MarketLichSister marketLich = MAPPER.readValue(json, MarketLichSister.class);
            assertNotNull(marketLich);
            assertNotNull(marketLich.getPayload(), "payload不应为空");
            var raw = marketLich.getPayload().getAuctions();
            assertFalse(raw.isEmpty(), "拍卖数据不应为空");
            System.out.println("  [lich] raw=" + raw.size() + " item=" + marketLich.getPayload().getItemName());

            // 模拟 MarketLichSisterUtils.processAuctionData() 过滤
            var filtered = filterAuctions(raw,
                    MarketLichSister.Auctions::getClosed,
                    MarketLichSister.Auctions::getVisible,
                    a -> a.getOwner() != null ? a.getOwner().getStatus() : null,
                    MarketLichSister.Auctions::getBuyoutPrice,
                    MarketLichSister.Auctions::getStartingPrice,
                    MarketLichSister.Auctions::getTopBid
            );
            assertFalse(filtered.isEmpty(), "过滤后应有在线用户拍卖");
            marketLich.getPayload()
                    .setAuctions(filtered)
                    .setItemName(lichSlug);
            System.out.println("  [lich] filtered=" + filtered.size());

            byte[] img = drawImagePlugin.drawMarketLichesImage(marketLich);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_liches.png"), img);
            System.out.println("[market_liches] OK " + img.length + "B slug=" + lichSlug);
        }
    }

    @Nested
    @DisplayName("9. 市场信条 — API → 过滤(closed/visible/online) → drawMarketSisterImage")
    class MarketSisters_ {
        @Test
        void test() throws Exception {
            assertNotNull(sisterSlug, "应从H2获取信条武器slug");
            String json = fetchAuctionJson("sister", sisterSlug);
            MarketLichSister marketSister = MAPPER.readValue(json, MarketLichSister.class);
            assertNotNull(marketSister);
            assertNotNull(marketSister.getPayload(), "payload不应为空");
            var raw = marketSister.getPayload().getAuctions();
            assertFalse(raw.isEmpty(), "拍卖数据不应为空");
            System.out.println("  [sister] raw=" + raw.size() + " item=" + marketSister.getPayload().getItemName());

            // 模拟 MarketLichSisterUtils.processAuctionData() 过滤
            var filtered = filterAuctions(raw,
                    MarketLichSister.Auctions::getClosed,
                    MarketLichSister.Auctions::getVisible,
                    a -> a.getOwner() != null ? a.getOwner().getStatus() : null,
                    MarketLichSister.Auctions::getBuyoutPrice,
                    MarketLichSister.Auctions::getStartingPrice,
                    MarketLichSister.Auctions::getTopBid
            );
            assertFalse(filtered.isEmpty(), "过滤后应有在线用户拍卖");
            marketSister.getPayload().setAuctions(filtered)
                    .setItemName(sisterSlug);
            System.out.println("  [sister] filtered=" + filtered.size());

            byte[] img = drawImagePlugin.drawMarketSisterImage(marketSister);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_sisters.png"), img);
            System.out.println("[market_sisters] OK " + img.length + "B slug=" + sisterSlug);
        }
    }

    @Nested
    @DisplayName("10. 市场订单 — MarketOrdersPlugin API+H2 → drawMarketOrdersImage")
    class MarketOrders_ {
        @Test
        void test() throws Exception {
            assertNotNull(orderSlug, "应从H2获取订单物品slug");
            // 从 H2 获取物品信息
            OrdersItemsInfo itemInfo = ordersItemInfoMap.get(orderSlug);
            assertNotNull(itemInfo, "应有物品信息: " + orderSlug);

            // 从 Market API 获取订单（使用 marketSendGet 设置 Platform 头）
            String url = "https://api.warframe.market/v2/orders/item/" + orderSlug;
            var body = HttpUtils.marketSendGet(url);
            System.out.println("  [orders] API HTTP " + body.code() + " body:" + body.body().length() + " chars");

            JsonNode root = MAPPER.readTree(body.body());
            // Warframe Market v2 返回格式: {"apiVersion":"...","data":[...]}
            JsonNode ordersNode = root.get("data");
            if (ordersNode == null || !ordersNode.isArray()) {
                ordersNode = root.at("/payload/orders");
            }
            assertTrue(ordersNode != null && ordersNode.isArray(),
                    "orders应有数据, response=" + body.body().substring(0, Math.min(200, body.body().length())));
            List<OrderWithUser> orderList = MAPPER.readValue(ordersNode.traverse(),
                    new TypeReference<List<OrderWithUser>>() {
                    });
            System.out.println("  [orders] raw orders=" + orderList.size());

            // 过滤在线用户、按价格排序、限8条
            List<OrderWithUser> filtered = orderList.stream()
                    .filter(o -> o.getUser() != null
                            && (o.getUser().getStatus() == MarketStatusEnum.INGAME
                            || o.getUser().getStatus() == MarketStatusEnum.ONLINE))
                    .sorted(Comparator.comparingInt(o -> o.getPlatinum() != null ? o.getPlatinum() : Integer.MAX_VALUE))
                    .limit(8)
                    .toList();
            assertFalse(filtered.isEmpty(), "应有在线订单");

            Orders orders = new Orders()
                    .setName(itemInfo.name)
                    .setForm(MarketPlatformEnum.PC)
                    .setIsBy(false)
                    .setIsMax(false)
                    .setDucats(itemInfo.ducats)
                    .setVaulted(itemInfo.vaulted)
                    .setMaxAmberStars(itemInfo.maxAmberStars)
                    .setMaxCyanStars(itemInfo.maxCyanStars)
                    .setBaseEndo(itemInfo.baseEndo)
                    .setReqMasteryRank(0)
                    .setTradingTax(0)
                    .setOrders(filtered);

            byte[] img = drawImagePlugin.drawMarketOrdersImage(orders);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("market_orders.png"), img);
            System.out.println("[market_orders] OK " + img.length + "B item=" + itemInfo.name + " orders=" + filtered.size());
        }
    }
}
