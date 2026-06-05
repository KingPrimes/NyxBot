package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.DailyDeals;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 每日特惠消息构建器
 * <p>
 * 负责构建Darvo每日特惠的通知消息
 * </p>
 */
@Slf4j
@Component
public class DailyDealsMessageBuilder implements MessageBuilder<DailyDeals> {

    private final DrawImagePlugin drawImagePlugin;


    public DailyDealsMessageBuilder(DrawImagePlugin drawImagePlugin) {
        this.drawImagePlugin = drawImagePlugin;
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<DailyDeals> event, MissionSubscribeUserCheckType rule) {
        DailyDeals deal = event.data();

        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        try {
            // 尝试生成警报图片（传入单个警报的列表）
            byte[] image = drawImagePlugin.drawDailyDealsImage(deal);
            builder.img(image);
        } catch (Exception e) {
            log.warn("生成每日特惠图片失败，使用文本消息: {}", e.getMessage());
            // 消息标题
            builder.text("\n━━━━━ Darvo每日特惠 ━━━━━");

            // 商品名称
            if (deal.getItem() != null && !deal.getItem().isEmpty()) {
                builder.text("\n🎁 商品: " + deal.getItem());
            }

            // 价格信息
            if (deal.getSalePrice() != null && deal.getOriginalPrice() != null) {
                builder.text("\n💰 售价: " + deal.getSalePrice() + " 白金");
                builder.text("\n💵 原价: " + deal.getOriginalPrice() + " 白金");

                // 计算折扣
                int discount = 0;
                if (deal.getOriginalPrice() > 0 && deal.getSalePrice() > 0) {
                    discount = 100 - (deal.getSalePrice() * 100 / deal.getOriginalPrice());
                }
                builder.text("\n🔥 折扣: " + discount + "%");
            }

            // 库存信息
            if (deal.getTotal() != null && deal.getSold() != null) {
                int remaining = deal.getTotal() - deal.getSold();
                builder.text("\n📦 库存: " + remaining + " / " + deal.getTotal());
            }

            // 过期时间
            if (deal.getExpiry() != null) {
                builder.text("\n⏰ 过期: " + deal.getTimeLeft());
            }

            builder.text("\n━━━━━━━━━━━━━━━━");

        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.DAILY_DEALS;
    }
}