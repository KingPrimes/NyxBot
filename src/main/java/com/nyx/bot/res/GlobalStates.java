package com.nyx.bot.res;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 用于接收 WarframeSocket 的返回值
 */
@NoArgsConstructor
@Data
public class GlobalStates {
    /**
     * 警报
     */
    @JsonProperty("alerts")
    private List<Alerts> alerts;
    /**
     * 仲裁
     */
    @JsonProperty("arbitration")
    private Arbitration arbitration;
    /**
     * 魔胎之境
     */
    @JsonProperty("cambionCycle")
    private CambionCycle cambionCycle;
    /**
     * 希图斯
     */
    @JsonProperty("cetusCycle")
    private CetusCycle cetusCycle;
    /**
     * 入侵双方的建筑情况
     */
    @JsonProperty("constructionProgress")
    private ConstructionProgress constructionProgress;
    /**
     * 每日特惠
     */
    @JsonProperty("dailyDeals")
    private List<DailyDeals> dailyDeals;
    /**
     * 地球
     */
    @JsonProperty("earthCycle")
    private EarthCycle earthCycle;
    /**
     * 活动
     */
    @JsonProperty("events")
    private List<Events> events;
    /**
     * 裂隙
     */
    @JsonProperty("fissures")
    private List<Fissures> fissures;


    //
    @JsonProperty("globalUpgrades")
    private List<GlobalUpgrades> globalUpgrades;

    /**
     * 入侵
     */
    @JsonProperty("invasions")
    private List<Invasions> invasions;
    /**
     * 新闻
     */
    @JsonProperty("news")
    private List<News> news;
    /**
     * 电波
     */
    @JsonProperty("nightwave")
    private Nightwave nightwave;
    /**
     * 大黄脸中枢的结合目标
     */
    @JsonProperty("simaris")
    private Simaris simaris;
    /**
     * 突击
     */
    @JsonProperty("sortie")
    private Sortie sortie;

    //执政官突击
    @JsonProperty("archonHunt")
    private ArchonHunt archonHunt;

    /**
     * 钢铁兑换轮换
     */
    @JsonProperty("steelPath")
    private SteelPath steelPath;
    /**
     * 集团任务
     */
    @JsonProperty("syndicateMissions")
    private List<SyndicateMissions> syndicateMissions;
    /**
     * 奥布山谷 轮换
     */
    @JsonProperty("vallisCycle")
    private VallisCycle vallisCycle;
    /**
     * 扎的曼轮换
     */
    @JsonProperty("zarimanCycle")
    private ZarimanCycle zarimanCycle;
    /**
     * 虚空商人
     */
    @JsonProperty("voidTrader")
    private VoidTrader voidTrader;

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"node", "tierNum", "missionType"})
    public static class Fissures {
        @JsonProperty("node")
        private String node;
        @JsonProperty("expired")
        private Boolean expired;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("missionType")
        private String missionType;
        @JsonProperty("missionKey")
        private String missionKey;
        @JsonProperty("nodeKey")
        private String nodeKey;
        @JsonProperty("tier")
        private String tier;
        @JsonProperty("tierNum")
        private Integer tierNum;
        @JsonProperty("enemy")
        private String enemy;
        @JsonProperty("enemyKey")
        private String enemyKey;
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("isHard")
        private Boolean isHard;
        @JsonProperty("isStorm")
        private Boolean isStorm;
        @JsonProperty("active")
        private Boolean active;
        @JsonProperty("startString")
        private String startString;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"start", "end", "upgrade"})
    public static class GlobalUpgrades {

        //开始时间
        @JsonProperty("start")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date start;

        //结束时间
        @JsonProperty("end")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date end;

        //加成类型
        @JsonProperty("upgrade")
        private String upgrade;

        //加成方式 乘/加
        @JsonProperty("operation")
        private String operation;

        //加成方式 x/+
        @JsonProperty("operationSymbol")
        private String operationSymbol;

        //加成倍数
        @JsonProperty("upgradeOperationValue")
        private Integer upgradeOperationValue;

        //是否关闭
        @JsonProperty("expired")
        private Boolean expired;

        //结束时间
        @JsonProperty("eta")
        private String eta;

        //具体字符串
        @JsonProperty("desc")
        private String desc;

    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"attacker", "attackerReward", "node", "defenderReward", "defender", "defendingFaction", "attackingFaction"})
    public static class Invasions {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("attacker")
        private Attacker attacker;
        @JsonProperty("attackerReward")
        private AttackerReward attackerReward;
        @JsonProperty("attackingFaction")
        private String attackingFaction;
        @JsonProperty("completed")
        private Boolean completed;
        @JsonProperty("count")
        private Integer count;
        @JsonProperty("defender")
        private Defender defender;
        @JsonProperty("defenderReward")
        private DefenderReward defenderReward;
        @JsonProperty("defendingFaction")
        private String defendingFaction;
        @JsonProperty("desc")
        private String desc;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("node")
        private String node;
        @JsonProperty("nodeKey")
        private String nodeKey;
        @JsonProperty("requiredRuns")
        private Integer requiredRuns;
        @JsonProperty("rewardTypes")
        private List<String> rewardTypes;
        @JsonProperty("startString")
        private String startString;
        @JsonProperty("vsInfestation")
        private Boolean vsInfestation;
        @JsonProperty("completion")
        private String completion;

        @NoArgsConstructor
        @Data
        public static class Attacker {
            @JsonProperty("reward")
            private Reward reward;
            @JsonProperty("faction")
            private String faction;
            @JsonProperty("factionKey")
            private String factionKey;

            @NoArgsConstructor
            @Data
            @EqualsAndHashCode(of = {"countedItems", "itemString"})
            public static class Reward {
                @JsonProperty("countedItems")
                private List<CountedItems> countedItems;
                @JsonProperty("thumbnail")
                private String thumbnail;
                @JsonProperty("color")
                private Integer color;
                @JsonProperty("credits")
                private Integer credits;
                @JsonProperty("asString")
                private String asString;
                @JsonProperty("itemString")
                private String itemString;

                @NoArgsConstructor
                @Data
                public static class CountedItems {
                    @JsonProperty("count")
                    private Integer count;
                    @JsonProperty("type")
                    private String type;
                }
            }
        }

        @NoArgsConstructor
        @Data
        @EqualsAndHashCode(of = {"countedItems", "itemString"})
        public static class AttackerReward {
            @JsonProperty("countedItems")
            private List<CountedItems> countedItems;
            @JsonProperty("thumbnail")
            private String thumbnail;
            @JsonProperty("color")
            private Integer color;
            @JsonProperty("credits")
            private Integer credits;
            @JsonProperty("asString")
            private String asString;
            @JsonProperty("itemString")
            private String itemString;

            @NoArgsConstructor
            @Data
            public static class CountedItems {
                @JsonProperty("count")
                private Integer count;
                @JsonProperty("type")
                private String type;
            }
        }

        @NoArgsConstructor
        @Data
        public static class Defender {
            @JsonProperty("reward")
            private Reward reward;
            @JsonProperty("faction")
            private String faction;
            @JsonProperty("factionKey")
            private String factionKey;

            @NoArgsConstructor
            @Data
            @EqualsAndHashCode(of = {"countedItems", "itemString"})
            public static class Reward {
                @JsonProperty("countedItems")
                private List<CountedItems> countedItems;
                @JsonProperty("thumbnail")
                private String thumbnail;
                @JsonProperty("color")
                private Integer color;
                @JsonProperty("credits")
                private Integer credits;
                @JsonProperty("asString")
                private String asString;
                @JsonProperty("itemString")
                private String itemString;

                @NoArgsConstructor
                @Data
                public static class CountedItems {
                    @JsonProperty("count")
                    private Integer count;
                    @JsonProperty("type")
                    private String type;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class DefenderReward {
            @JsonProperty("countedItems")
            private List<CountedItems> countedItems;
            @JsonProperty("thumbnail")
            private String thumbnail;
            @JsonProperty("color")
            private Integer color;
            @JsonProperty("credits")
            private Integer credits;
            @JsonProperty("asString")
            private String asString;
            @JsonProperty("itemString")
            private String itemString;

            @NoArgsConstructor
            @Data
            public static class CountedItems {
                @JsonProperty("count")
                private Integer count;
                @JsonProperty("type")
                private String type;
            }
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"message"})
    public static class News {

        @JsonProperty("date")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date date;
        /**
         * 展示图片
         */
        @JsonProperty("imageLink")
        private String imageLink;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("primeAccess")
        private Boolean primeAccess;
        @JsonProperty("stream")
        private Boolean stream;
        @JsonProperty("translations")
        private Translations translations;

        @JsonProperty("link")
        private String link;
        @JsonProperty("update")
        private Boolean update;
        @JsonProperty("id")
        private String id;
        @JsonProperty("asString")
        private String asString;
        @JsonProperty("message")
        private String message;
        @JsonProperty("priority")
        private Boolean priority;
        @JsonProperty("startDate")
        private String startDate;

        @NoArgsConstructor
        @Data
        public static class Translations {
            @JsonProperty("es")
            private String es;
            @JsonProperty("zh")
            private String zh;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"nodes", "jobs"})
    public static class SyndicateMissions {
        @JsonProperty("nodes")
        private List<String> nodes;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("jobs")
        private List<Jobs> jobs;
        @JsonProperty("syndicate")
        private String syndicate;
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;

        @NoArgsConstructor
        @Data
        @EqualsAndHashCode(of = {"rewardPool"})
        public static class Jobs {
            @JsonProperty("activation")
            private String activation;
            @JsonProperty("expiry")
            private String expiry;
            @JsonProperty("rewardPool")
            private List<String> rewardPool;
            @JsonProperty("type")
            private String type;
            @JsonProperty("enemyLevels")
            private List<Integer> enemyLevels;
            @JsonProperty("standingStages")
            private List<Integer> standingStages;
            @JsonProperty("minMR")
            private Integer minMR;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(exclude = {"activation", "expiry"})
    public static class Arbitration {
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("node")
        private String node;
        @JsonProperty("enemy")
        private String enemy;
        @JsonProperty("type")
        private String type;
        @JsonProperty("archwing")
        private Boolean archwing;
        @JsonProperty("sharkwing")
        private Boolean sharkwing;
        @JsonProperty("etc")
        private String etc;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(exclude = {"expiry", "activation", "timeLeft"})
    public static class CambionCycle {
        @JsonProperty("id")
        private String id;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("active")
        private String active;
        @JsonProperty("timeLeft")
        private String timeLeft;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"isDay", "isCetus", "state"})
    public static class CetusCycle {
        @JsonProperty("id")
        private String id;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("isDay")
        private Boolean isDay;
        @JsonProperty("state")
        private String state;
        @JsonProperty("timeLeft")
        private String timeLeft;
        @JsonProperty("isCetus")
        private Boolean isCetus;
        @JsonProperty("shortString")
        private String shortString;
    }

    @NoArgsConstructor
    @Data
    public static class ConstructionProgress {
        @JsonProperty("id")
        private String id;
        @JsonProperty("fomorianProgress")
        private String fomorianProgress;
        @JsonProperty("razorbackProgress")
        private String razorbackProgress;
        @JsonProperty("unknownProgress")
        private String unknownProgress;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"isDay", "state"})
    public static class EarthCycle {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("isDay")
        private Boolean isDay;
        @JsonProperty("state")
        private String state;
        @JsonProperty("timeLeft")
        private String timeLeft;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"season", "activeChallenges"})
    public static class Nightwave {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("rewardTypes")
        private List<String> rewardTypes;
        @JsonProperty("season")
        private Integer season;
        @JsonProperty("tag")
        private String tag;
        @JsonProperty("phase")
        private Integer phase;
        @JsonProperty("activeChallenges")
        private List<ActiveChallenges> activeChallenges;

        @NoArgsConstructor
        @Data
        @EqualsAndHashCode(of = {"title", "desc"})
        public static class ActiveChallenges {
            @JsonProperty("id")
            private String id;
            @JsonProperty("activation")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            private Date activation;
            @JsonProperty("expiry")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
            private Date expiry;
            @JsonProperty("isDaily")
            private Boolean isDaily;
            @JsonProperty("isElite")
            private Boolean isElite;
            @JsonProperty("title")
            private String title;
            @JsonProperty("desc")
            private String desc;
            @JsonProperty("reputation")
            private Integer reputation;
            @JsonProperty("active")
            private Boolean active;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Simaris {
        @JsonProperty("target")
        private String target;
        @JsonProperty("isTargetActive")
        private Boolean isTargetActive;
        @JsonProperty("asString")
        private String asString;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"boss", "missions"})
    public static class ArchonHunt {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("startString")
        private String startString;
        @JsonProperty("actice")
        private Boolean actice;
        @JsonProperty("rewardPool")
        private String rewardPool;
        @JsonProperty("missions")
        private List<Mission> missions;
        @JsonProperty("boss")
        private String boss;
        @JsonProperty("faction")
        private String faction;
        @JsonProperty("factionKey")
        private String factionKey;
        @JsonProperty("expired")
        private Boolean expired;
        @JsonProperty("eta")
        private String eta;

        @Data
        @NoArgsConstructor
        public static class Mission {
            @JsonProperty("node")
            private String node;
            @JsonProperty("nodeKey")
            private String nodeKey;
            @JsonProperty("type")
            private String type;
            @JsonProperty("typeKey")
            private String typeKey;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"variants", "boss"})
    public static class Sortie {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("rewardPool")
        private String rewardPool;
        @JsonProperty("variants")
        private List<Variants> variants;
        @JsonProperty("boss")
        private String boss;
        @JsonProperty("faction")
        private String faction;
        @JsonProperty("expired")
        private Boolean expired;
        @JsonProperty("eta")
        private String eta;

        @NoArgsConstructor
        @Data
        public static class Variants {
            @JsonProperty("node")
            private String node;
            @JsonProperty("boss")
            private String boss;
            @JsonProperty("missionType")
            private String missionType;
            @JsonProperty("planet")
            private String planet;
            @JsonProperty("modifier")
            private String modifier;
            @JsonProperty("modifierDescription")
            private String modifierDescription;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"currentReward"})
    public static class SteelPath {
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("currentReward")
        private CurrentReward currentReward;
        @JsonProperty("isReward")
        private String isReward;
        @JsonProperty("nexReward")
        private String nexReward;
        @JsonProperty("etc")
        private String etc;
        @JsonProperty("remaining")
        private String remaining;
        @JsonProperty("cost")
        private Integer cost;
        @JsonProperty("rotation")
        private List<Rotation> rotation;
        @JsonProperty("evergreens")
        private List<Evergreens> evergreens;

        @NoArgsConstructor
        @Data
        public static class CurrentReward {
            @JsonProperty("name")
            private String name;
            @JsonProperty("cost")
            private Integer cost;
        }

        @NoArgsConstructor
        @Data
        public static class Rotation {
            @JsonProperty("name")
            private String name;
            @JsonProperty("cost")
            private Integer cost;
        }

        @NoArgsConstructor
        @Data
        public static class Evergreens {
            @JsonProperty("name")
            private String name;
            @JsonProperty("cost")
            private Integer cost;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"isWarm", "state"})
    public static class VallisCycle {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("timeLeft")
        private String timeLeft;
        @JsonProperty("isWarm")
        private Boolean isWarm;
        @JsonProperty("state")
        private String state;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"isCorpus", "state"})
    public static class ZarimanCycle {
        @JsonProperty("id")
        private String id;

        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;

        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;

        @JsonProperty("bountiesEndDate")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date bountiesEndDate;

        @JsonProperty("isCorpus")
        private Boolean isCorpus;

        @JsonProperty("state")
        private String state;

        @JsonProperty("timeLeft")
        private String timeLeft;

        @JsonProperty("shortString")
        private String shortString;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"inventory", "location", "active"})
    public static class VoidTrader {
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("character")
        private String character;
        @JsonProperty("location")
        private String location;
        @JsonProperty("inventory")
        private List<Inventory> inventory;
        @JsonProperty("psId")
        private String psId;
        @JsonProperty("active")
        private Boolean active;
        @JsonProperty("startString")
        private String startString;
        @JsonProperty("endString")
        private String endString;

        @NoArgsConstructor
        @Data
        public static class Inventory {
            @JsonProperty("item")
            private String item;
            @JsonProperty("ducats")
            private Integer ducats;
            @JsonProperty("credits")
            private Integer credits;
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"mission", "expired", "rewardTypes"})
    public static class Alerts {
        @JsonProperty("mission")
        private Mission mission;
        @JsonProperty("expired")
        private Boolean expired;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("rewardTypes")
        private List<String> rewardTypes;

        @NoArgsConstructor
        @Data
        public static class Mission {
            @JsonProperty("reward")
            private Reward reward;
            @JsonProperty("node")
            private String node;
            @JsonProperty("faction")
            private String faction;
            @JsonProperty("maxEnemyLevel")
            private Integer maxEnemyLevel;
            @JsonProperty("minEnemyLevel")
            private Integer minEnemyLevel;
            @JsonProperty("maxWaveNum")
            private Integer maxWaveNum;
            @JsonProperty("type")
            private String type;
            @JsonProperty("nightmare")
            private Boolean nightmare;
            @JsonProperty("archwingRequired")
            private Boolean archwingRequired;
            @JsonProperty("isSharkwing")
            private Boolean isSharkwing;
            @JsonProperty("enemySpec")
            private String enemySpec;
            @JsonProperty("levelOverride")
            private String levelOverride;
            @JsonProperty("advancedSpawners")
            private List<String> advancedSpawners;
            @JsonProperty("requiredItems")
            private List<String> requiredItems;
            @JsonProperty("consumeRequiredItems")
            private Boolean consumeRequiredItems;
            @JsonProperty("leadersAlwaysAllowed")
            private Boolean leadersAlwaysAllowed;
            @JsonProperty("levelAuras")
            private List<String> levelAuras;

            @NoArgsConstructor
            @Data
            public static class Reward {
                @JsonProperty("countedItems")
                private List<CountedItems> countedItems;
                @JsonProperty("thumbnail")
                private String thumbnail;
                @JsonProperty("color")
                private Integer color;
                @JsonProperty("credits")
                private Integer credits;
                @JsonProperty("asString")
                private String asString;
                @JsonProperty("items")
                private List<String> items;
                @JsonProperty("itemString")
                private String itemString;

                @NoArgsConstructor
                @Data
                public static class CountedItems {
                    @JsonProperty("count")
                    private Integer count;
                    @JsonProperty("type")
                    private String type;
                }
            }
        }
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"item"})
    public static class DailyDeals {
        @JsonProperty("sold")
        private Integer sold;
        @JsonProperty("item")
        private String item;
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("eta")
        private String eta;
        @JsonProperty("originalPrice")
        private Integer originalPrice;
        @JsonProperty("salePrice")
        private Integer salePrice;
        @JsonProperty("discount")
        private Integer discount;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("id")
        private String id;
    }

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(of = {"description", "rewards"})
    public static class Events {

        @JsonProperty("id")
        private String id;
        @JsonProperty("activation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date activation;
        @JsonProperty("startString")
        private String startString;
        @JsonProperty("expiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date expiry;
        @JsonProperty("active")
        private Boolean active;
        @JsonProperty("maximumScore")
        private Integer maximumScore;
        @JsonProperty("currentScore")
        private Integer currentScore;
        @JsonProperty("smallInterval")
        private Integer smallInterval;
        @JsonProperty("largeInterval")
        private Integer largeInterval;
        @JsonProperty("jobs")
        private List<Jobs> jobs;
        @JsonProperty("previousJobs")
        private List<PreviousJobs> previousJobs;
        @JsonProperty("previousId")
        private String previousId;
        @JsonProperty("faction")
        private String faction;
        @JsonProperty("description")
        private String description;
        @JsonProperty("tooltip")
        private String tooltip;
        @JsonProperty("node")
        private String node;
        @JsonProperty("concurrentNodes")
        private List<String> concurrentNodes;
        @JsonProperty("victimNode")
        private String victimNode;
        @JsonProperty("scoreLocTag")
        private String scoreLocTag;
        @JsonProperty("rewards")
        private List<Rewards> rewards;
        @JsonProperty("expired")
        private Boolean expired;
        @JsonProperty("health")
        private Integer health;
        @JsonProperty("affiliatedWith")
        private String affiliatedWith;
        @JsonProperty("progressTotal")
        private Integer progressTotal;
        @JsonProperty("showTotalAtEndOfMission")
        private Boolean showTotalAtEndOfMission;
        @JsonProperty("isPersonal")
        private Boolean isPersonal;
        @JsonProperty("isCommunity")
        private Boolean isCommunity;
        @JsonProperty("regionDrops")
        private List<String> regionDrops;
        @JsonProperty("archwingDrops")
        private List<String> archwingDrops;
        @JsonProperty("asString")
        private String asString;
        @JsonProperty("metadata")
        private List<Integer> completionBonuses;
        @JsonProperty("scoreVar")
        private String scoreVar;
        @JsonProperty("altExpiry")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date altExpiry;
        @JsonProperty("altActivation")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date altActivation;

        @NoArgsConstructor
        @Data
        @EqualsAndHashCode(of = {"countedItems", "asString", "itemString"})
        public static class Rewards {
            @JsonProperty("countedItems")
            private List<CountedItems> countedItems;
            @JsonProperty("thumbnail")
            private String thumbnail;
            @JsonProperty("color")
            private Integer color;
            @JsonProperty("credits")
            private Integer credits;
            @JsonProperty("asString")
            private String asString;
            @JsonProperty("items")
            private List<String> items;
            @JsonProperty("itemString")
            private String itemString;

            @NoArgsConstructor
            @Data
            public static class CountedItems {
                @JsonProperty("count")
                private Integer count;
                @JsonProperty("type")
                private String type;
            }
        }

        @NoArgsConstructor
        @lombok.Data
        public static class Jobs {
            @JsonProperty("id")
            private String id;
            @JsonProperty("rewardPool")
            private List<?> rewardPool;
            @JsonProperty("type")
            private String type;
            @JsonProperty("enemyLevels")
            private List<Integer> enemyLevels;
            @JsonProperty("standingStages")
            private List<Integer> standingStages;
            @JsonProperty("minMR")
            private Integer minMR;
            @JsonProperty("expiry")
            private String expiry;
        }

        @NoArgsConstructor
        @lombok.Data
        public static class PreviousJobs {
            @JsonProperty("id")
            private String id;
            @JsonProperty("rewardPool")
            private List<?> rewardPool;
            @JsonProperty("type")
            private String type;
            @JsonProperty("enemyLevels")
            private List<Integer> enemyLevels;
            @JsonProperty("standingStages")
            private List<Integer> standingStages;
            @JsonProperty("minMR")
            private Integer minMR;
            @JsonProperty("expiry")
            private String expiry;
        }

    }

}
