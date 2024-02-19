package com.nyx.bot.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Locale;

/**
 * Warframe.market Riven查询 URL拼接
 */
@Data
@AllArgsConstructor
public class MarketRivenParameter {
    //url拼接物品名
    String urlName;
    //物品名称
    String itemName;
    //正向词条以,分割
    String positiveStats;
    //负面词条 none 无  has 有
    String negativeStats;
    //极性
    Polarity polarity;
    //最低段位
    Integer masteryRankMin;
    //最高段位
    Integer masteryRankMax;
    //类别 direct 售卖  auction 拍卖 默认全部
    Policy buyoutPolicy;
    //排序方式  price_asc 价格正序, price_desc 价格倒序, damage_asc 伤害正序, damage_desc 伤害倒序
    SortBy sortBy;

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        url.append("https://api.warframe.market/v1/auctions/search?type=riven&weapon_url_name=")
                .append(getUrlName());
        if (!getPositiveStats().trim().isEmpty()) {
            url.append("&positive_stats=").append(getPositiveStats());
        }
        if (!getBuyoutPolicy().equals(Policy.ANY)) {
            url.append("&buyout_policy=").append(getBuyoutPolicy().name());
        }
        if (!getNegativeStats().trim().isEmpty()) {
            url.append("&negative_stats=").append(getNegativeStats());
        }
        if (getMasteryRankMin() < 7 || getMasteryRankMin() > 16) {
            url.append("&mastery_rank_min=7");
        } else {
            url.append("&mastery_rank_min=").append(getMasteryRankMin());
        }
        if (getMasteryRankMax() < 7 || getMasteryRankMax() > 16) {
            url.append("&mastery_rank_max=16");
        } else {
            url.append("&mastery_rank_max=").append(getMasteryRankMax());
        }
        url
                .append("&polarity=").append(getPolarity().name())
                .append("&sort_by=").append(getSortBy().name());
        return url.toString().toLowerCase(Locale.ROOT);
    }

    /**
     * 极性
     */
    public enum Polarity {
        ANY,
        MADURAI,
        VAZARIN,
        NARAMON
    }

    /**
     * 类别： direct 售卖  auction 拍卖 any 全部
     */
    public enum Policy {
        ANY,
        // 售卖
        DIRECT,
        // 拍卖
        AUCTION
    }

    /**
     * 排序方式： price_asc 价格正序, price_desc 价格倒序, damage_asc 伤害正序, damage_desc 伤害倒序
     */
    public enum SortBy {
        //价格正序
        PRICE_ASC,
        //价格倒序
        PRICE_DESC,
        //伤害正序
        DAMAGE_ASC,
        //伤害倒序
        DAMAGE_DESC
    }


}
