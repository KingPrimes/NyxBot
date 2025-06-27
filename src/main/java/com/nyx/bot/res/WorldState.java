package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.worldstate.*;
import lombok.Data;

import java.util.List;

@Data
public class WorldState {
    // 裂隙任务
    @JsonProperty("ActiveMissions")
    private List<ActiveMission> activeMissions;
    // 警报任务
    @JsonProperty("Alerts")
    private List<Alert> alerts;
    @JsonProperty("BuildLabel")
    private String buildLabel;
    @JsonProperty("ConstructionProjects")
    private List<ConstructionProjects> constructionProjects;
    // 每日特惠
    @JsonProperty("DailyDeals")
    private List<DailyDeals> dailyDeals;
    // 双衍王境
    @JsonProperty("EndlessXpChoices")
    private List<EndlessXpChoices> endlessXpChoices;
    // 新闻
    @JsonProperty("Events")
    private List<Event> events;
    //
    @JsonProperty("ExperimentRecommended")
    private List<ExperimentRecommended> experimentRecommended;
    // 精选氏族
    @JsonProperty("FeaturedGuilds")
    private List<FeaturedGuilds> featuredGuilds;
    // 闪购
    @JsonProperty("FlashSales")
    private List<FlashSale> flashSales;
    @JsonProperty("ForceLogoutVersion")
    private Integer forceLogoutVersion;
    @JsonProperty("GlobalUpgrades")
    private List<GlobalUpgrade> globalUpgrades;
    // 活动
    @JsonProperty("Goals")
    private List<Goal> goals;
    //
    @JsonProperty("HubEvents")
    private List<HubEvents> hubEvents;
    // 游戏内商城
    @JsonProperty("InGameMarket")
    private InGameMarket inGameMarket;
    // 入侵
    @JsonProperty("Invasions")
    private List<Invasion> invasions;
    // 1999日历
    @JsonProperty("KnownCalendarSeasons")
    private List<KnownCalendarSeasons> knownCalendarSeasons;
    //
    @JsonProperty("LibraryInfo")
    private LibraryInfo libraryInfo;
    // 执刑官猎杀
    @JsonProperty("LiteSorties")
    private List<LiteSorite> liteSorties;
    //
    @JsonProperty("MobileVersion")
    private String mobileVersion;
    //
    @JsonProperty("NodeOverrides")
    private List<NodeOverride> nodeOverrides;
    // 瓦奇娅
    @JsonProperty("PrimeVaultTraders")
    private List<PrimeVaultTrader> primeVaultTraders;
    //
    @JsonProperty("ProjectPct")
    private List<Float> projectPct;
    //
    @JsonProperty("SeasonInfo")
    private List<SeasonInfo> seasonInfo;
    // 突击
    @JsonProperty("Sorties")
    private List<Sortie> sorties;
    // 集团任务
    @JsonProperty("SyndicateMissions")
    private List<SyndicateMission> syndicateMissions;
    @JsonProperty("Time")
    private Long time;
    @JsonProperty("Tmp")
    private String tmp;
    @JsonProperty("Version")
    private Integer version;
    // 虚空风暴 九重天裂隙
    @JsonProperty("VoidStorms")
    private List<VoidStorms> voidStorms;
    // 虚空商人
    @JsonProperty("VoidTraders")
    private List<VoidTrader> voidTraders;
    // 世界种子
    private String worldSeed;

}
