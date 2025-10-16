package com.nyx.bot.modules.warframe.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.res.enums.SyndicateEnum;
import com.nyx.bot.modules.warframe.res.worldstate.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;
@SuppressWarnings("unused")
@Data
public class WorldState {
    // 地球循环
    EarthCycle earthCycle;

    // 夜灵平原
    CetusCycle cetusCycle;

    // 魔胎之境
    CambionCycle cambionCycle;

    // 奥布山谷 轮换
    VallisCycle vallisCycle;

    // 裂隙任务
    @JsonProperty("ActiveMissions")
    List<ActiveMission> activeMissions;

    // 警报任务
    @JsonProperty("Alerts")
    List<Alert> alerts;

    @JsonProperty("BuildLabel")
    String buildLabel;

    @JsonProperty("ConstructionProjects")
    List<ConstructionProjects> constructionProjects;

    // 每日特惠
    @JsonProperty("DailyDeals")
    List<DailyDeals> dailyDeals;

    // 双衍王境 奖励
    @JsonProperty("EndlessXpChoices")
    List<EndlessXpChoices> endlessXpChoices;

    // 双衍王境 轮换
    DuviriCycle duviriCycle;

    // 新闻
    @JsonProperty("Events")
    List<Event> events;

    //
    @JsonProperty("ExperimentRecommended")
    List<ExperimentRecommended> experimentRecommended;

    // 精选氏族
    @JsonProperty("FeaturedGuilds")
    List<FeaturedGuilds> featuredGuilds;

    // 闪购
    @JsonProperty("FlashSales")
    List<FlashSale> flashSales;

    @JsonProperty("ForceLogoutVersion")
    Integer forceLogoutVersion;

    @JsonProperty("GlobalUpgrades")
    List<GlobalUpgrade> globalUpgrades;

    // 活动
    @JsonProperty("Goals")
    List<Goal> goals;

    //
    @JsonProperty("HubEvents")
    List<HubEvents> hubEvents;

    // 游戏内商城
    @JsonProperty("InGameMarket")
    InGameMarket inGameMarket;

    // 入侵
    @JsonProperty("Invasions")
    List<Invasion> invasions;

    // 1999日历
    @JsonProperty("KnownCalendarSeasons")
    List<KnownCalendarSeasons> knownCalendarSeasons;

    //
    @JsonProperty("LibraryInfo")
    LibraryInfo libraryInfo;

    // 执刑官猎杀
    @JsonProperty("LiteSorties")
    List<LiteSorite> liteSorties;

    //
    @JsonProperty("MobileVersion")
    String mobileVersion;

    //
    @JsonProperty("NodeOverrides")
    List<NodeOverride> nodeOverrides;

    // 瓦奇娅
    @JsonProperty("PrimeVaultTraders")
    List<PrimeVaultTrader> primeVaultTraders;

    //
    @JsonProperty("ProjectPct")
    List<Float> projectPct;

    // 电波
    @JsonProperty("SeasonInfo")
    SeasonInfo seasonInfo;

    // 突击
    @JsonProperty("Sorties")
    List<Sortie> sorties;

    // 集团任务
    @JsonProperty("SyndicateMissions")
    List<SyndicateMission> syndicateMissions;

    // 扎的曼轮换
    ZarimanCycle zarimanCycle;

    @JsonProperty("Time")
    Long time;

    @JsonProperty("Tmp")
    String tmp;

    @JsonProperty("Version")
    Integer version;

    // 虚空风暴 九重天裂隙
    @JsonProperty("VoidStorms")
    List<VoidStorms> voidStorms;

    // 虚空商人
    @JsonProperty("VoidTraders")
    List<VoidTrader> voidTraders;

    // 世界种子
    @JsonProperty("WorldSeed")
    String worldSeed;

    @JsonProperty("SteelPath")
    SteelPathOffering steelPath = new SteelPathOffering();

    @JsonIgnore
    public EarthCycle getEarthCycle() {
        return new EarthCycle();
    }
    @JsonIgnore
    public CetusCycle getCetusCycle() {
        return new CetusCycle(getBountiesEndDate(SyndicateEnum.CetusSyndicate));
    }
    @JsonIgnore
    public CambionCycle getCambionCycle() {
        return new CambionCycle(getCetusCycle());
    }
    @JsonIgnore
    public VallisCycle getVallisCycle() {
        return new VallisCycle();
    }
    @JsonIgnore
    public DuviriCycle getDuviriCycle() {
        return new DuviriCycle(this.getEndlessXpChoices());
    }
    @JsonIgnore
    public ZarimanCycle getZarimanCycle() {
        return new ZarimanCycle(getBountiesEndDate(SyndicateEnum.ZarimanSyndicate));
    }

    @JsonIgnore
    private Instant getBountiesEndDate(SyndicateEnum key) {
        return this.getSyndicateMissions()
                .stream()
                .filter(s -> s.getTag() != null && s.getTag().equals(key))
                .findFirst()
                .map(s -> s.getExpiry().getEpochSecond())
                .orElse(Instant.now());
    }
}
