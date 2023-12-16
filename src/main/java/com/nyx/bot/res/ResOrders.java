package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用于接收
 * Warframe Market <a href="https://api.warframe.market/v1/items/mirage_prime_systems/orders?include=item">...</a> API接口的返回值
 */
@NoArgsConstructor
@Data
public class ResOrders {

    @JsonProperty("payload")
    private Payload payload;
    @JsonProperty("include")
    private Include include;

    @NoArgsConstructor
    @Data
    public static class Payload {
        @JsonProperty("orders")
        private List<Payload.Orders> orders;

        @NoArgsConstructor
        @Data
        public static class Orders {
            @JsonProperty("id")
            private String id;
            @JsonProperty("platinum")
            private Integer platinum;
            @JsonProperty("quantity")
            private Integer quantity;
            @JsonProperty("order_type")
            private String orderType;
            @JsonProperty("platform")
            private String platform;
            @JsonProperty("region")
            private String region;
            @JsonProperty("creation_date")
            private String creationDate;
            @JsonProperty("last_update")
            private String lastUpdate;
            @JsonProperty("subtype")
            private String subtype;
            @JsonProperty("visible")
            private Boolean visible;
            @JsonProperty("user")
            private Payload.Orders.User user;

            @NoArgsConstructor
            @Data
            public static class User {
                @JsonProperty("id")
                private String id;
                @JsonProperty("ingame_name")
                private String ingameName;
                @JsonProperty("status")
                private String status;
                @JsonProperty("region")
                private String region;
                @JsonProperty("reputation")
                private Integer reputation;
                @JsonProperty("avatar")
                private String avatar;
                @JsonProperty("last_seen")
                private String lastSeen;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class Include {
        @JsonProperty("item")
        private Include.Item item;

        @NoArgsConstructor
        @Data
        public static class Item {
            @JsonProperty("id")
            private String id;
            @JsonProperty("items_in_set")
            private List<Include.Item.ItemsInSet> itemsInSet;

            @NoArgsConstructor
            @Data
            public static class ItemsInSet {
                @JsonProperty("id")
                private String id;
                @JsonProperty("url_name")
                private String urlName;
                @JsonProperty("icon")
                private String icon;
                @JsonProperty("icon_format")
                private String iconFormat;
                @JsonProperty("thumb")
                private String thumb;
                @JsonProperty("sub_icon")
                private String subIcon;
                @JsonProperty("mod_max_rank")
                private Integer modMaxRank;
                @JsonProperty("subtypes")
                private List<String> subtypes;
                @JsonProperty("tags")
                private List<String> tags;
                @JsonProperty("ducats")
                private Integer ducats;
                @JsonProperty("quantity_for_set")
                private Integer quantityForSet;
                @JsonProperty("set_root")
                private Boolean setRoot;
                @JsonProperty("mastery_level")
                private Integer masteryLevel;
                @JsonProperty("rarity")
                private String rarity;
                @JsonProperty("trading_tax")
                private Integer tradingTax;
                @JsonProperty("en")
                private Include.Item.ItemsInSet.En en;
                @JsonProperty("ru")
                private Include.Item.ItemsInSet.Ru ru;
                @JsonProperty("ko")
                private Include.Item.ItemsInSet.Ko ko;
                @JsonProperty("fr")
                private Include.Item.ItemsInSet.Fr fr;
                @JsonProperty("de")
                private Include.Item.ItemsInSet.De de;
                @JsonProperty("sv")
                private Include.Item.ItemsInSet.Sv sv;
                @JsonProperty("zh_hant")
                private Include.Item.ItemsInSet.ZhHant zhHant;
                @JsonProperty("zh_hans")
                private Include.Item.ItemsInSet.ZhHans zhHans;
                @JsonProperty("pt")
                private Include.Item.ItemsInSet.Pt pt;
                @JsonProperty("es")
                private Include.Item.ItemsInSet.Es es;
                @JsonProperty("pl")
                private Include.Item.ItemsInSet.Pl pl;

                @NoArgsConstructor
                @Data
                public static class En {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.En.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Ru {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Ru.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Ko {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Ko.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Fr {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Fr.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class De {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.De.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Sv {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Sv.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class ZhHant {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.ZhHant.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class ZhHans {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.ZhHans.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Pt {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Pt.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Es {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Es.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }

                @NoArgsConstructor
                @Data
                public static class Pl {
                    @JsonProperty("item_name")
                    private String itemName;
                    @JsonProperty("description")
                    private String description;
                    @JsonProperty("wiki_link")
                    private String wikiLink;
                    @JsonProperty("drop")
                    private List<Include.Item.ItemsInSet.Pl.Drop> drop;

                    @NoArgsConstructor
                    @Data
                    public static class Drop {
                        @JsonProperty("name")
                        private String name;
                        @JsonProperty("link")
                        private String link;
                    }
                }
            }
        }
    }
}
