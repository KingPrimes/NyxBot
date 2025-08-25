package com.nyx.bot.modules.warframe.res;

import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.SpringUtils;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class Ducats {

    Previous payload;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("payload", payload)
                .toString();
    }

    @Data
    public static class Ducat {
        /**
         * 时间
         */

        LocalDateTime dateTime;

        /**
         * 杜卡币
         */

        Integer ducats;
        /**
         * 1白金=?杜卡币
         */

        Double ducatsPerPlatinum;
        /**
         * 1白金=?杜卡币 实时
         */

        Double ducatsPerPlatinumWa;
        /**
         * 当前ID
         */

        String id;
        /**
         * 物品Id
         */

        String item;
        /**
         * 中位数
         */
        Integer median;
        /**
         * 平台价值
         */

        Float platWorth;
        /**
         * 日变动
         */
        Integer positionChangeDay;
        /**
         * 周变动
         */
        Integer positionChangeWeek;
        /**
         * 月变动
         */
        Integer positionChangeMonth;
        /**
         * 存货
         */
        Integer volume;
        /**
         * 均价
         */
        Double waPrice;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("dateTime", dateTime)
                    .append("ducats", ducats)
                    .append("ducatsPerPlatinum", ducatsPerPlatinum)
                    .append("ducatsPerPlatinumWa", ducatsPerPlatinumWa)
                    .append("id", id)
                    .append("item", item)
                    .append("median", median)
                    .append("platWorth", platWorth)
                    .append("positionChangeDay", positionChangeDay)
                    .append("positionChangeWeek", positionChangeWeek)
                    .append("positionChangeMonth", positionChangeMonth)
                    .append("volume", volume)
                    .append("waPrice", waPrice)
                    .toString();
        }
    }

    @Data
    public static class Previous {
        List<Ducat> previousDay;
        List<Ducat> previousHour;

        public Map<String, List<Ducat>> getSilverDump() {
            Map<String, List<Ducat>> silverDump = new java.util.HashMap<>();
            List<Ducat> day = this.previousDay.stream().filter(ducat -> ducat.ducats >= 45 && ducat.ducats < 100)
                    .peek(d -> d.setItem(SpringUtils.getBean(OrdersItemsRepository.class).findById(d.getItem()).orElse(new OrdersItems()).getName()))
                    .sorted((o1, o2) -> o2.ducatsPerPlatinumWa.compareTo(o1.ducatsPerPlatinumWa))
                    .limit(10)
                    .toList();
            List<Ducat> hour = this.previousHour.stream().filter(ducat -> ducat.ducats >= 45 && ducat.ducats < 100)
                    .peek(d -> d.setItem(SpringUtils.getBean(OrdersItemsRepository.class).findById(d.getItem()).orElse(new OrdersItems()).getName()))
                    .sorted((o1, o2) -> o2.ducatsPerPlatinumWa.compareTo(o1.ducatsPerPlatinumWa))
                    .limit(10)
                    .toList();
            silverDump.put("day", day);
            silverDump.put("hour", hour);
            return silverDump;
        }

        public Map<String, List<Ducat>> getGodDump() {
            Map<String, List<Ducat>> silverDump = new java.util.HashMap<>();
            List<Ducat> day = this.previousDay.stream().filter(ducat -> ducat.ducats == 100)
                    .peek(d -> d.setItem(SpringUtils.getBean(OrdersItemsRepository.class).findById(d.getItem()).orElse(new OrdersItems()).getName()))
                    .sorted((o1, o2) -> o2.ducatsPerPlatinumWa.compareTo(o1.ducatsPerPlatinumWa))
                    .limit(10)
                    .toList();
            List<Ducat> hour = this.previousHour.stream().filter(ducat -> ducat.ducats == 100)
                    .peek(d -> d.setItem(SpringUtils.getBean(OrdersItemsRepository.class).findById(d.getItem()).orElse(new OrdersItems()).getName()))
                    .sorted((o1, o2) -> o2.ducatsPerPlatinumWa.compareTo(o1.ducatsPerPlatinumWa))
                    .limit(10)
                    .toList();
            silverDump.put("day", day);
            silverDump.put("hour", hour);
            return silverDump;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("previousDay", previousDay)
                    .append("previousHour", previousHour)
                    .toString();
        }
    }
}
