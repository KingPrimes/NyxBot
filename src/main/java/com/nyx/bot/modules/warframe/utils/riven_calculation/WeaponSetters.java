package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.utils.RivenMatcherUtil;
import io.github.kingprimes.model.RivenAnalyseTrendModel;

import java.util.function.ToDoubleFunction;

public class WeaponSetters {

    private static void setWeaponAttr(RivenAnalyseTrendCompute.Attribute attr,
                                      RivenAnalyseTrendModel.Attribute model,
                                      double v,
                                      RivenAnalyseTrend trend,
                                      int i,
                                      ToDoubleFunction<RivenAnalyseTrend> baseExtractor) {
        double baseVal = baseExtractor.applyAsDouble(trend);
        // 歧视词条由数值正负决定属性
        boolean isNag = RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute());
        if (RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())) {
            // 歧视词条显示百分比范围
            double low = attr.getLowAttribute(baseVal, v, i, attr.getNag(), isNag);
            double high = attr.getHighAttribute(baseVal, v, i, attr.getNag(), isNag);
            model.setLowAttr(String.format("%.1f", low));
            model.setHighAttr(String.format("%.1f", high));
        } else {
            model.setLowAttr(String.valueOf(attr.getLowAttribute(baseVal, v, i, attr.getNag(), isNag)));
            model.setHighAttr(String.valueOf(attr.getHighAttribute(baseVal, v, i, attr.getNag(), isNag)));
        }
    }

    public static void setPistols(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                  double v, RivenAnalyseTrend trend, int i) {
        setWeaponAttr(attr, model, v, trend, i, RivenAnalyseTrend::getPistol);
    }

    public static void setLongGuns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                   double v, RivenAnalyseTrend trend, int i) {
        setWeaponAttr(attr, model, v, trend, i, RivenAnalyseTrend::getRifle);
    }

    public static void setMelee(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                double v, RivenAnalyseTrend trend, int i) {
        setWeaponAttr(attr, model, v, trend, i, RivenAnalyseTrend::getMelle);
    }

    public static void setSpaceGuns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                    double v, RivenAnalyseTrend trend, int i) {
        setWeaponAttr(attr, model, v, trend, i, RivenAnalyseTrend::getArchwing);
    }

    public static void setSpaceMelee(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                     double v, RivenAnalyseTrend trend, int i) {
        setSpaceGuns(attr, model, v, trend, i);
    }

    public static void setSpecialItems(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                       double v, RivenAnalyseTrend trend, int i) {
        // 显赫武器不出紫卡，无需计算
    }

    public static void setCrewShipWeapons(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                          double v, RivenAnalyseTrend trend, int i) {
        // 星舰武器不出紫卡，无需计算
    }

    public static void setSentinelWeapons(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                          double v, RivenAnalyseTrend trend, int i) {
        setLongGuns(attr, model, v, trend, i);
    }

    public static void setShotguns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model,
                                   double v, RivenAnalyseTrend trend, int i) {
        setWeaponAttr(attr, model, v, trend, i, RivenAnalyseTrend::getShotgun);
    }
}
