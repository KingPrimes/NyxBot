package com.nyx.bot.modules.warframe.plugin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.entity.exprot.NightWave;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.entity.exprot.reward.Reward;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.enums.FissureTypeEnum;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.utils.SyndicateMissionsUtils;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.defaultdraw.DefaultDrawImagePlugin;
import io.github.kingprimes.model.Arbitration;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.RarityEnum;
import io.github.kingprimes.model.enums.SyndicateEnum;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * WorldState 完整流程集成测试。
 * <p>链路：status.json(API数据) → ObjectMapper → WorldState → H2数据库翻译 → 绘图 → temp/test-images/</p>
 */
@DisplayName("WorldState 完整流程集成测试")
@SuppressWarnings("unchecked")
class WorldStateFullPipelineTest {

    static final Path OUT = Path.of("temp", "test-images");
    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    // H2 JDBC 连接
    static final String DB_URL = "jdbc:h2:file:./data/H2;MODE=MySQL;AUTO_SERVER=TRUE;PAGE_SIZE=16";
    static final String DB_USER = "sa", DB_PASS = "sa";

    // 从 H2 加载的真实数据
    private static Map<String, String> translations; // uniqueName → 中文名
    private static Map<String, Nodes> nodeMap;        // uniqueName → Nodes
    private static Map<String, NightWave> nightWaveMap; // uniqueName → NightWave
    private static Map<String, RewardPool> rewardPoolMap; // uniqueName → RewardPool
    private static Map<String, String> weaponTranslations; // englishName → 中文名

    private static WorldState worldState;
    private static List<Arbitration> arbitrationList;
    private static MockedStatic<WarframeCache> mockedCache;
    private static MockedStatic<CacheUtils> mockedCacheUtils;
    private static DefaultDrawImagePlugin drawImagePlugin;

    @BeforeAll
    static void beforeAll() throws Exception {
        drawImagePlugin = new DefaultDrawImagePlugin();

        translations = loadTranslations();
        nodeMap = loadNodes();
        nightWaveMap = loadNightWaves();
        rewardPoolMap = loadRewardPools();
        weaponTranslations = loadWeapons();

        // 从真实 API 获取 WorldState 数据
        var body = HttpUtils.sendGet(ApiUrl.WARFRAME_WORLD_STATE);
        System.out.println("[API] HTTP " + body.code() + " body:" + body.body().length() + " chars");
        worldState = MAPPER.readValue(body.body(), WorldState.class);

        // 从本地 Base64 文件加载仲裁预测数据（预存30天数据）
        String arbBase64 = Files.readString(Path.of("data", "arbitration"));
        byte[] arbJson = java.util.Base64.getDecoder().decode(arbBase64);
        arbitrationList = MAPPER.readValue(arbJson, new TypeReference<List<Arbitration>>() {
        });
        System.out.println("[local] arbitrationList: " + arbitrationList.size() + " 条");

        mockedCache = mockStatic(WarframeCache.class);
        mockedCache.when(WarframeCache::getWarframeStatus).thenReturn(worldState);

        mockedCacheUtils = mockStatic(CacheUtils.class);
        mockedCacheUtils.when(() -> CacheUtils.get(anyString(), any(), any(Class.class))).thenReturn(null);
        mockedCacheUtils.when(() -> CacheUtils.set(anyString(), any(), any(), any(), any())).then(invocation -> null);

        Files.createDirectories(OUT);
    }

    @AfterAll
    static void afterAll() {
        if (mockedCache != null) mockedCache.close();
        if (mockedCacheUtils != null) mockedCacheUtils.close();
    }

    // ======================== H2 数据加载 ========================

    private static Map<String, String> loadTranslations() throws Exception {
        Map<String, String> m = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT unique_name, name FROM state_translation")) {
            while (rs.next()) m.put(rs.getString("unique_name"), rs.getString("name"));
        }
        System.out.println("[H2] state_translation: " + m.size() + " 条");
        return m;
    }

    private static Map<String, Nodes> loadNodes() throws Exception {
        Map<String, Nodes> m = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM nodes")) {
            while (rs.next()) {
                Nodes n = new Nodes();
                n.setUniqueName(rs.getString("unique_name"));
                n.setName(rs.getString("name"));
                n.setSystemName(rs.getString("system_name"));
                n.setFactionIndex(rs.getInt("faction_index"));
                n.setMissionIndex(rs.getInt("mission_index"));
                m.put(n.getUniqueName(), n);
            }
        }
        System.out.println("[H2] nodes: " + m.size() + " 条");
        return m;
    }

    private static Map<String, NightWave> loadNightWaves() throws Exception {
        Map<String, NightWave> m = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM night_wave")) {
            while (rs.next()) {
                NightWave nw = new NightWave();
                nw.setUniqueName(rs.getString("unique_name"));
                nw.setName(rs.getString("name"));
                nw.setDescription(rs.getString("description"));
                nw.setStanding(rs.getInt("standing"));
                nw.setRequired(rs.getInt("required"));
                m.put(nw.getUniqueName(), nw);
            }
        }
        System.out.println("[H2] night_wave: " + m.size() + " 条");
        return m;
    }

    private static Map<String, RewardPool> loadRewardPools() throws Exception {
        // 先加载奖池-奖励关联
        Map<String, java.util.List<Reward>> poolRewards = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT rpr.rewards, r.item, r.item_count, r.rarity " +
                             "FROM reward_pool_reward rpr JOIN reward r ON rpr.reward_id = r.id")) {
            while (rs.next()) {
                String poolId = rs.getString("rewards");
                Reward reward = new Reward();
                reward.setItem(rs.getString("item"));
                reward.setItemCount(rs.getInt("item_count"));
                reward.setRarity(RarityEnum.values()[rs.getInt("rarity")]);
                poolRewards.computeIfAbsent(poolId, k -> new java.util.ArrayList<>()).add(reward);
            }
        }
        // 构建 RewardPool 对象
        Map<String, RewardPool> m = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT unique_name FROM reward_pool")) {
            while (rs.next()) {
                String id = rs.getString("unique_name");
                RewardPool rp = new RewardPool();
                rp.setUniqueName(id);
                rp.setRewards(poolRewards.getOrDefault(id, java.util.List.of()));
                m.put(id, rp);
            }
        }
        System.out.println("[H2] reward_pool: " + m.size() + " 条 (reward_pool_reward: " +
                poolRewards.values().stream().mapToInt(java.util.List::size).sum() + " 条)");
        return m;
    }

    private static Map<String, String> loadWeapons() throws Exception {
        Map<String, String> m = new HashMap<>();
        try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT english_name, name FROM weapons WHERE english_name IS NOT NULL")) {
            while (rs.next()) {
                m.put(rs.getString("english_name"), rs.getString("name"));
            }
        }
        System.out.println("[H2] weapons: " + m.size() + " 条");
        return m;
    }

    // ======================== Repo 工厂 ========================

    private static StateTranslation toSt(String id, String name) {
        StateTranslation st = new StateTranslation();
        st.setUniqueName(id);
        st.setName(name);
        return st;
    }

    private StateTranslationRepository stRepo() {
        StateTranslationRepository repo = mock(StateTranslationRepository.class);
        lenient().when(repo.findByUniqueName(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            // 精确匹配
            if (translations.containsKey(key)) return Optional.of(toSt(key, translations.get(key)));
            // 后缀匹配 (RIGHT(uniqueName, LENGTH(key)))
            for (var e : translations.entrySet()) {
                if (e.getKey().endsWith(key)) return Optional.of(toSt(e.getKey(), e.getValue()));
            }
            return Optional.empty();
        });
        return repo;
    }

    private WeaponsRepository weaponsRepo() {
        WeaponsRepository repo = mock(WeaponsRepository.class);
        lenient().when(repo.findByEnglishName(anyString())).thenAnswer(inv -> {
            String name = inv.getArgument(0);
            String chinese = weaponTranslations.get(name);
            if (chinese != null) {
                Weapons w = new Weapons();
                w.setEnglishName(name);
                w.setName(chinese);
                return Optional.of(w);
            }
            return Optional.empty();
        });
        return repo;
    }

    private NodesRepository nodesRepo() {
        NodesRepository repo = mock(NodesRepository.class);
        lenient().when(repo.findById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            Nodes db = nodeMap.get(id);
            if (db != null) return Optional.of(db);
            Nodes fb = new Nodes();
            fb.setName(id);
            fb.setSystemName("?");
            fb.setFactionIndex(0);
            fb.setMissionIndex(1);
            return Optional.of(fb);
        });
        return repo;
    }

    private NightWaveRepository nwRepo() {
        NightWaveRepository repo = mock(NightWaveRepository.class);
        lenient().when(repo.findById(anyString())).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            NightWave db = nightWaveMap.get(id);
            if (db != null) return Optional.of(db);
            return Optional.empty();
        });
        return repo;
    }

    private RewardPoolRepository rpRepo() {
        RewardPoolRepository repo = mock(RewardPoolRepository.class);
        lenient().when(repo.findById(anyString())).thenAnswer(inv ->
                Optional.ofNullable(rewardPoolMap.get(inv.getArgument(0))));
        return repo;
    }

    // ======================== 公共组件 ========================

    private WorldStateUtils createWorldStateUtils() {
        return new WorldStateUtils(stRepo(), weaponsRepo(), nodesRepo(), nwRepo());
    }

    private SyndicateMissionsUtils createSyndicateUtils() {
        return new SyndicateMissionsUtils(drawImagePlugin, rpRepo(), stRepo());
    }

    // ======================== 测试 ========================
    @Nested
    @DisplayName("1. 警报 — item/location 数据库翻译")
    class Alerts {
        @Test
        void test() throws Exception {
            var wsu = createWorldStateUtils();
            var alerts = wsu.getAlerts();
            assertFalse(alerts.isEmpty());
            var reward = alerts.get(0).getMissionInfo().getMissionReward();
            if (reward.getItems() != null && !reward.getItems().isEmpty())
                assertFalse(reward.getItems().get(0).startsWith("/Lotus/"), "item应被翻译");
            byte[] img = drawImagePlugin.drawAlertsImage(alerts);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("alerts.png"), img);
            System.out.println("[alerts] OK " + img.length + "B " + alerts.size() + "条");
        }
    }

    @Nested
    @DisplayName("2. 平原时间 — AllCycle")
    class AllCycle {
        @Test
        void test() throws Exception {
            var cycle = createWorldStateUtils().getAllCycle();
            assertNotNull(cycle);
            byte[] img = drawImagePlugin.drawAllCycleImage(cycle);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("allcycle.png"), img);
            System.out.println("[allcycle] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("3. 仲裁 — Arbitration（API数据 → 当前仲裁绘图）")
    class Arbitration_ {
        @Test
        void test() throws Exception {
            assertFalse(arbitrationList.isEmpty(), "仲裁API应返回数据");
            long milli = Instant.now().getEpochSecond();
            Arbitration a = arbitrationList.stream()
                    .filter(ar -> ar.getExpiry().getEpochSecond() - milli > 0)
                    .min(Comparator.comparingLong(obj -> obj.getExpiry().getEpochSecond() - milli))
                    .orElse(null);
            assertNotNull(a, "应有当前进行中的仲裁");
            assertNotNull(a.getNode(), "node不应为空");
            assertNotNull(a.getType(), "type不应为空");
            assertNotNull(a.getEnemy(), "enemy不应为空");
            byte[] img = drawImagePlugin.drawArbitrationImage(a);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("arbitration.png"), img);
            System.out.println("[arbitration] OK " + img.length + "B node=" + a.getNode() + " type=" + a.getType() + " enemy=" + a.getEnemyName());
        }
    }

    @Nested
    @DisplayName("3b. 仲裁表 — Arbitrations（有价值仲裁列表绘图）")
    class Arbitrations_ {
        @Test
        void test() throws Exception {
            assertFalse(arbitrationList.isEmpty(), "仲裁API应返回数据");
            List<Arbitration> worthList = arbitrationList.stream()
                    .filter(Arbitration::isWorth)
                    .limit(5)
                    .toList();
            assertFalse(worthList.isEmpty(), "应有标记为有价值的仲裁");
            byte[] img = drawImagePlugin.drawArbitrationsImage(worthList);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("arbitrations.png"), img);
            System.out.println("[arbitrations] OK " + img.length + "B " + worthList.size() + "条");
            worthList.forEach(a -> System.out.println("  " + a.getNode() + " | " + a.getType() + " | " + a.getEnemyName()));
        }
    }

    @Nested
    @DisplayName("4. 每日特惠 — item 数据库翻译")
    class DailyDeals {
        @Test
        void test() throws Exception {
            var deals = createWorldStateUtils().getDailyDeals();
            if (!deals.isEmpty()) {
                assertFalse(deals.get(0).getItem().startsWith("/Lotus/"), "item应被翻译");
                byte[] img = drawImagePlugin.drawDailyDealsImage(deals.get(0));
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("dailydeals.png"), img);
                System.out.println("[dailydeals] OK " + img.length + "B " + deals.size() + "条");
            }
        }
    }

    @Nested
    @DisplayName("5. 双衍王境 — 武器ID翻译")
    class DuviriCycle {
        @Test
        void test() throws Exception {
            var cycle = createWorldStateUtils().getDuvalierCycle();
            assertNotNull(cycle);
            assertNotNull(cycle.getChoices());
            assertFalse(cycle.getChoices().isEmpty());
            byte[] img = drawImagePlugin.drawDuviriCycleImage(cycle);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("duviricycle.png"), img);
            System.out.println("[duviricycle] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("6. 裂隙 — node ID → 节点名+阵营")
    class Fissure {
        @Test
        void active() throws Exception {
            var list = createWorldStateUtils().getFissure(FissureTypeEnum.ACTIVE_MISSION);
            assertFalse(list.isEmpty());
            assertNotNull(list.get(0).getFaction());
            byte[] img = drawImagePlugin.drawActiveMissionImage(list);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("fissure_active.png"), img);
            System.out.println("[fissure-active] OK " + img.length + "B " + list.size() + "条");
        }

        @Test
        void steelPath() throws Exception {
            var list = createWorldStateUtils().getFissure(FissureTypeEnum.STEEL_PATH);
            assertFalse(list.isEmpty());
            assertNotNull(list.get(0).getFaction());
            byte[] img = drawImagePlugin.drawActiveMissionImage(list);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("fissure_steelpath.png"), img);
            System.out.println("[fissure-steelpath] OK " + img.length + "B " + list.size() + "条");
        }

        @Test
        void voidStorms() throws Exception {
            var list = createWorldStateUtils().getFissure(FissureTypeEnum.VOID_STORMS);
            assertFalse(list.isEmpty());
            byte[] img = drawImagePlugin.drawActiveMissionImage(list);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("fissure_voidstorms.png"), img);
            System.out.println("[fissure-voidstorms] OK " + img.length + "B " + list.size() + "条");
        }
    }

    @Nested
    @DisplayName("7. 入侵 — 奖励item/节点 数据库翻译")
    class Invasion_ {
        @Test
        void test() throws Exception {
            var list = createWorldStateUtils().getInvasions();
            if (!list.isEmpty()) {
                byte[] img = drawImagePlugin.drawInvasionImage(list);
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("invasion.png"), img);
                System.out.println("[invasion] OK " + img.length + "B " + list.size() + "条");
            }
        }
    }

    @Nested
    @DisplayName("8. 1999日历 — WorldStateUtils 完整处理链（processDays + 月日分组 + 翻译）")
    class KnownCalendar {
        @Test
        void test() throws Exception {
            var list = createWorldStateUtils().getKnownCalendarSeasons();
            assertNotNull(list);
            assertFalse(list.isEmpty(), "日历数据不应为空");
            // 验证 processSeason 已处理：days 应为 null，monthDays 应有数据
            var first = list.get(0);
            assertNull(first.getDays(), "processSeason 后 days 应为 null");
            assertNotNull(first.getMonthDays(), "processSeason 后 monthDays 应有数据");
            assertFalse(first.getMonthDays().isEmpty());
            byte[] img = drawImagePlugin.drawKnownCalendarSeasonsImage(list);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("knowncalendar.png"), img);
            System.out.println("[knowncalendar] OK " + img.length + "B " + list.size() + "个季节");
        }
    }

    @Nested
    @DisplayName("9. 执刑官猎杀 — node 数据库翻译")
    class LiteSortie_ {
        @Test
        void test() throws Exception {
            var list = createWorldStateUtils().getLiteSorite();
            if (!list.isEmpty()) {
                byte[] img = drawImagePlugin.drawLiteSoriteImage(list.get(0));
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("litesorite.png"), img);
                System.out.println("[litesorite] OK " + img.length + "B");
            }
        }
    }

    @Nested
    @DisplayName("10. 电波 — challenge 数据库翻译（214条 night_wave）")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NightWave_ {
        private WorldStateUtils wsu;

        @BeforeEach
        void setUp() {
            wsu = createWorldStateUtils();
        }

        @Test
        void translateAndDraw() throws Exception {
            var info = wsu.getSeasonInfo();
            assertNotNull(info);
            // 验证挑战已被翻译（name 不应为空/null）
            if (info.getActiveChallenges() != null && !info.getActiveChallenges().isEmpty()) {
                var ch = info.getActiveChallenges().get(0);
                assertNotNull(ch.getName(), "挑战名应从数据库翻译");
                assertNotNull(ch.getRequired(), "required 应从数据库读取");
                assertFalse(ch.getName().startsWith("/Lotus/"), "挑战名不应是原始ID");
                System.out.println("  " + ch.getName() + " (required=" + ch.getRequired() +
                        ", standing=" + ch.getStanding() + ")");
            }
            byte[] img = drawImagePlugin.drawSeasonInfoImage(info);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("nightwave.png"), img);
            System.out.println("[nightwave] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("11. 突击 — node 数据库翻译")
    class Sorties_ {
        @Test
        void test() throws Exception {
            var list = createWorldStateUtils().getSorties();
            if (!list.isEmpty()) {
                byte[] img = drawImagePlugin.drawSortiesImage(list.get(0));
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("sorties.png"), img);
                System.out.println("[sorties] OK " + img.length + "B");
            }
        }
    }

    @Nested
    @DisplayName("12. 钢铁奖励")
    class SteelPath_ {
        @Test
        void test() throws Exception {
            var offering = worldState.getSteelPath();
            assertNotNull(offering);
            byte[] img = drawImagePlugin.drawSteelPath(offering);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("steelpath.png"), img);
            System.out.println("[steelpath] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("13. 赏金 — SyndicateMissionsUtils 完整处理链（jobType翻译+奖池翻译）")
    class Syndicate_ {
        private SyndicateMissionsUtils syndicateUtils;

        @BeforeEach
        void setUp() {
            syndicateUtils = createSyndicateUtils();
        }

        @Test
        void entrati() throws Exception {
            byte[] img = syndicateUtils.postSyndicateEntratiImage(SyndicateEnum.EntratiSyndicate);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("syndicate_entrati.png"), img);
            System.out.println("[syndicate-entrati] OK " + img.length + "B");
        }

        @Test
        void ostrons() throws Exception {
            byte[] img = syndicateUtils.postSyndicateEntratiImage(SyndicateEnum.CetusSyndicate);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("syndicate_ostrons.png"), img);
            System.out.println("[syndicate-ostrons] OK " + img.length + "B");
        }

        @Test
        void solaris() throws Exception {
            byte[] img = syndicateUtils.postSyndicateEntratiImage(SyndicateEnum.SolarisSyndicate);
            assertTrue(img.length > 0);
            Files.write(OUT.resolve("syndicate_solaris.png"), img);
            System.out.println("[syndicate-solaris] OK " + img.length + "B");
        }
    }

    @Nested
    @DisplayName("14. 虚空商人 — 货物/节点 数据库翻译")
    class VoidTrader_ {
        @Test
        void test() throws Exception {
            var list = createWorldStateUtils().getVoidTraders();
            if (!list.isEmpty()) {
                var t = list.get(0);
                if (t.getManifest() != null && !t.getManifest().isEmpty())
                    assertFalse(t.getManifest().get(0).getItem().startsWith("/Lotus/"), "货物名应被翻译");
                byte[] img = drawImagePlugin.drawVoidTraderImage(list);
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("void.png"), img);
                System.out.println("[void] OK " + img.length + "B");
            }
        }
    }

    @Nested
    @DisplayName("15. 深层征服 — Conquest（硬编码中文映射）")
    class Conquest_ {
        @Test
        void test() throws Exception {
            var list = worldState.getConquests();
            assertNotNull(list);
            if (!list.isEmpty()) {
                byte[] img = drawImagePlugin.drawConquestImage(list);
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("conquest.png"), img);
                System.out.println("[conquest] OK " + img.length + "B " + list.size() + "条");
            } else {
                System.out.println("[conquest] 当前无数据");
            }
        }
    }

    @Nested
    @DisplayName("16. 深层下降 — Descent（硬编码中文映射）")
    class Descent_ {
        @Test
        void test() throws Exception {
            var list = worldState.getDescents();
            assertNotNull(list);
            if (!list.isEmpty()) {
                byte[] img = drawImagePlugin.drawDescentImage(list);
                assertTrue(img.length > 0);
                Files.write(OUT.resolve("descent.png"), img);
                System.out.println("[descent] OK " + img.length + "B " + list.size() + "条");
            } else {
                System.out.println("[descent] 当前无数据");
            }
        }
    }
}
