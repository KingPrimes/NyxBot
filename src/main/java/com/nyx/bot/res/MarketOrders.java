package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Warframe orders查询结果
 */
@Data
public class MarketOrders {
    Payload payload;
    Include include;
    String code;

    @Data
    public static class Payload {
        List<Orders> orders;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("orders", getOrders())
                    .toString();
        }
    }

    @Data
    public static class User {
        /**
         * 声誉
         */
        Integer reputation;
        /**
         * 地区
         */
        String region;
        /**
         * 上一次在线时间
         */
        @JsonProperty("last_seen")
        String lastSeen;
        /**
         * 用户名
         */
        @JsonProperty("ingame_name")
        String ingameName;
        /**
         * 用户头像
         */
        String avatar;
        /**
         * 用户状态
         */
        String status;
        /**
         * UUID
         */
        String id;


        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("reputation", getReputation())
                    .append("region", getRegion())
                    .append("last_seen", getLastSeen())
                    .append("ingame_name", getIngameName())
                    .append("avatar", getAvatar())
                    .append("status", getStatus())
                    .append("id", getId())
                    .toString();
        }
    }

    @Data
    public static class Orders {
        /**
         * 是否开启订单
         */
        Boolean visible;
        /**
         * 白金
         */
        Integer platinum;
        /**
         * 数量
         */
        Integer quantity;
        /**
         * 买卖类型
         */
        @JsonProperty("order_type")
        String orderType;
        /**
         * 用户
         */
        User user;
        /**
         * 平台
         */
        String platform;
        /**
         * 地区
         */
        String region;
        /**
         * 订单创建日期
         */
        @JsonProperty("creation_date")
        String creationDate;
        /**
         * 最后一次修改日期
         */
        @JsonProperty("last_update")
        String lastUpdate;
        /**
         * UUID
         */
        String id;
        /**
         * 等级
         */
        Integer modRank;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("visible", getVisible())
                    .append("platinum", getPlatinum())
                    .append("quantity", getQuantity())
                    .append("order_type", getOrderType())
                    .append("user", getUser())
                    .append("platform", getPlatform())
                    .append("region", getRegion())
                    .append("creation_date", getCreationDate())
                    .append("last_update", getLastUpdate())
                    .append("id", getId())
                    .append("modRank", getModRank())
                    .toString();
        }
    }

    @Data
    public static class Include {
        Item item;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("item", getItem())
                    .toString();
        }
    }

    @Data
    public static class Item {
        String id;

        @JsonProperty("items_in_set")
        List<ItemsInSet> itemsInSet;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("id", getId())
                    .append("items_in_set", getItemsInSet())
                    .toString();
        }
    }

    @Data
    public static class ItemsInSet {
        //图片地址
        String thumb;
        //url查询名称
        @JsonProperty("url_name")
        String urlName;
        //标签
        List<String> tags;
        //段位等级
        @JsonProperty("mastery_level")
        Integer masteryLevel;
        //图片icon
        String icon;
        //交易税
        @JsonProperty("trading_tax")
        Integer tradingTax;
        @JsonProperty("sub_icon")
        String subIcon;
        //物品id
        @JsonProperty("id")
        String id;
        //价值多少杜卡币
        @JsonProperty("ducats")
        Integer ducats;
        //icon来自哪里
        @JsonProperty("icon_format")
        String iconFormat;
        @JsonProperty("set_root")
        Boolean setRoot;
        @JsonProperty("zh-hans")
        Laguage zhHans;
        //mod最大等级
        @JsonProperty("mod_max_rank")
        Integer modMaxRank;
        //稀有程度
        @JsonProperty("rarity")
        String rarity;


        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("thumb", getThumb())
                    .append("urlName", getUrlName())
                    .append("tags", getTags())
                    .append("masteryLevel", getMasteryLevel())
                    .append("icon", getIcon())
                    .append("tradingTax", getTradingTax())
                    .append("subIcon", getSubIcon())
                    .append("id", getId())
                    .append("ducats", getDucats())
                    .append("iconFormat", getIconFormat())
                    .append("setRoot", getSetRoot())
                    .append("zhhans", getZhHans())
                    .append("modMaxRank", getModMaxRank())
                    .append("rarity", getRarity())
                    .toString();
        }
    }

    @Data
    public static class Laguage {
        //物品名称
        @JsonProperty("item_name")
        String itemName;
        //物品介绍
        String description;
        //Wiki地址
        @JsonProperty("wiki_link")
        String wikiLink;
        //掉落来源
        List<Drop> drop;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("item_name", getItemName())
                    .append("description", getDescription())
                    .append("wiki_link", getWikiLink())
                    .append("drop", getDrop())
                    .toString();
        }
    }

    @Data
    public static class Drop {
        String name;
        String link;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("name", getName())
                    .append("link", getLink())
                    .toString();
        }
    }

}
