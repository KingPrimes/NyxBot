package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询赤毒/信条武器拍卖市场数据
 */
@Data
public class MarketLichSister {

    @JsonProperty("payload")
    private Payload payload;

    @NoArgsConstructor
    @Data
    public static class Payload {
        @JsonProperty("auctions")
        private List<Auctions> auctions;

        @NoArgsConstructor
        @Data
        public static class Auctions {
            /*买断价格*/
            @JsonProperty("buyout_price")
            private Integer buyoutPrice;

            @JsonProperty("note")
            private String note;
            /*是否可见*/
            @JsonProperty("visible")
            private Boolean visible;
            /*物品*/
            @JsonProperty("item")
            private Item item;
            /*起拍价*/
            @JsonProperty("starting_price")
            private Integer startingPrice;
            /*声望*/
            @JsonProperty("minimal_reputation")
            private Integer minimalReputation;
            /*卖家/买家*/
            @JsonProperty("owner")
            private Owner owner;
            /*平台*/
            @JsonProperty("platform")
            private String platform;
            /*顶点是否关闭*/
            @JsonProperty("closed")
            private Boolean closed;
            /*最高出价*/
            @JsonProperty("top_bid")
            private Integer topBid;
            @JsonProperty("winner")
            private Object winner;
            @JsonProperty("is_marked_for")
            private Object isMarkedFor;
            @JsonProperty("marked_operation_at")
            private Object markedOperationAt;
            /*创建时间*/
            @JsonProperty("created")
            private LocalDateTime created;
            /*修改时间*/
            @JsonProperty("updated")
            private LocalDateTime updated;
            /**/
            @JsonProperty("note_raw")
            private String noteRaw;
            /*是否买断*/
            @JsonProperty("is_direct_sell")
            private Boolean isDirectSell;
            /*订单ID*/
            @JsonProperty("id")
            private String id;
            /*是否是私人*/
            @JsonProperty("private")
            private Boolean privateX;

            @NoArgsConstructor
            @Data
            public static class Item {
                /*物品类型*/
                @JsonProperty("type")
                private String type;
                /*加成*/
                @JsonProperty("damage")
                private Integer damage;
                /*武器名称*/
                @JsonProperty("weapon_url_name")
                private String weaponUrlName;
                /**
                 * 是否携带幻纹
                 */
                @JsonProperty("having_ephemera")
                private Boolean havingEphemera;
                /*元素*/
                @JsonProperty("element")
                private String element;

            }
        }
    }

}
