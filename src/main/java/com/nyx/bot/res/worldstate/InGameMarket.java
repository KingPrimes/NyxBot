package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class InGameMarket {

    @JsonProperty("LandingPage")
    private LandingPage landingPage;

    public enum CategoryName {
        NEW_PLAYER,
        NEW,
        COMMUNITY,
        HEIRLOOM,
        TENNOGEN,
        SALE,
        WISH_LIST,
        PREMIUM_BUNDLES,

    }

    @Data
    public static class LandingPage {
        @JsonProperty("Categories")
        private List<Category> categories;
    }

    @Data
    public static class Category {
        // 分类名称
        @JsonProperty("CategoryName")
        private CategoryName categoryName;
        // 子分类名称
        @JsonProperty("Name")
        private String name;
        // newplayer popular
        @JsonProperty("Icon")
        private String icon;
        // 添加到菜单
        @JsonProperty("AddToMenu")
        private Boolean addToMenu;
        @JsonProperty("Items")
        private List<String> items;
    }
}
