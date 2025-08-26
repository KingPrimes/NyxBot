package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.SpringUtils;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 金银垃圾
 */
@Setter
public class Ducats {
    private List<Ducat> ducats;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE).append("ducats", ducats).toString();
    }

    /**
     * 获取银垃圾列表
     */
    public List<Ducat> getSilverDump() {
        if (this.ducats.isEmpty()) return new ArrayList<>();
        List<Ducat> silver = new ArrayList<>();
        for (Ducat ducat : this.ducats) {
            if (ducat.getDucats() == 45) silver.add(ducat);
        }
        silver.sort((((o1, o2) -> o2.getDucatsPerPlatinumWa().compareTo(o1.getDucatsPerPlatinumWa()))));
        this.ducats = silver;
        return formatDucats();
    }

    /**
     * 获取金垃圾列表
     */
    public List<Ducat> getGodDump() {
        if (this.ducats.isEmpty()) return new ArrayList<>();
        List<Ducat> silver = new ArrayList<>();
        for (Ducat ducat : this.ducats) {
            if (ducat.getDucats() == 100) silver.add(ducat);
        }
        silver.sort((((o1, o2) -> o2.getDucatsPerPlatinumWa().compareTo(o1.getDucatsPerPlatinumWa()))));
        this.ducats = silver;
        return formatDucats();
    }

    /**
     * 格式化并限制 数量
     *
     * @return 格式化之后的数据
     */
    public List<Ducat> formatDucats() {
        List<Ducat> ducatList = new ArrayList<>();
        OrdersItemsRepository itemsRepository = SpringUtils.getBean(OrdersItemsRepository.class);
        int i = 0;
        for (Ducat ducat : this.ducats) {
            if (i >= 5) {
                break;
            }
            itemsRepository.findById(ducat.item).ifPresent(item -> ducat.setItemName(item.getName()));
            ducatList.add(ducat);
            i++;
        }
        return ducatList;
    }

    @Data
    public static class Ducat {
        /**
         * 时间
         */
        @JsonProperty("datetime")
        private String datetime;
        /**
         * 月变动
         */
        @JsonProperty("position_change_month")
        private Integer positionChangeMonth;
        /**
         * 周变动
         */
        @JsonProperty("position_change_week")
        private Integer positionChangeWeek;
        /**
         * 日变动
         */
        @JsonProperty("position_change_day")
        private Integer positionChangeDay;
        /**
         * 平台价值
         */
        @JsonProperty("plat_worth")
        private String platWorth;
        /**
         * 存货
         */
        @JsonProperty("volume")
        private Integer volume;
        /**
         * 1白金=?杜卡币
         */
        @JsonProperty("ducats_per_platinum")
        private String ducatsPerPlatinum;
        /**
         * 1白金=?杜卡币 实时
         */
        @JsonProperty("ducats_per_platinum_wa")
        private Double ducatsPerPlatinumWa;
        /**
         * 杜卡币
         */
        @JsonProperty("ducats")
        private Integer ducats;
        /**
         * 物品Id
         */
        @JsonProperty("item")
        private String item;
        /**
         * 中位数
         */
        @JsonProperty("median")
        private Integer median;
        /**
         * 均价
         */
        @JsonProperty("wa_price")
        private String waPrice;
        /**
         * 当前ID
         */
        @JsonProperty("id")
        private String id;

        /**
         * 物品名称
         */
        @JsonProperty("item_name")
        private String itemName;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE).append("datetime", datetime).append("positionChangeMonth", positionChangeMonth).append("positionChangeWeek", positionChangeWeek).append("positionChangeDay", positionChangeDay).append("platWorth", platWorth).append("volume", volume).append("ducatsPerPlatinum", ducatsPerPlatinum).append("ducatsPerPlatinumWa", ducatsPerPlatinumWa).append("ducats", ducats).append("item", item).append("median", median).append("waPrice", waPrice).append("id", id).toString();
        }

    }
}
