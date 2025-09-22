package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.utils.RivenMatcherUtil;

public class WeaponSetters {

    /**
     * 次要武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setPistols(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getPistol(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getPistol(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * 主武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setLongGuns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getRifle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getRifle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * 近战武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setMelee(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getMelle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getMelle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * Archwing武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setSpaceGuns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getArchwing(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getArchwing(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * Archwing近战武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setSpaceMelee(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getArchwing(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getArchwing(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * 显赫武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setSpecialItems(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        setRivenAnalyseTrend(attr, model, v, rivenAnalyseTrend, i);
    }

    /**
     * 星舰武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setCrewShipWeapons(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        setRivenAnalyseTrend(attr, model, v, rivenAnalyseTrend, i);
    }

    /**
     * 守护武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setSentinelWeapons(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getRifle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getRifle(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    /**
     * 霰弹枪武器
     *
     * @param attr              武器属性
     * @param model             武器属性模型
     * @param v                 武器属性值
     * @param rivenAnalyseTrend 武器分析趋势
     * @param i                 武器属性索引
     */
    public static void setShotguns(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {
        model.setLowAttr(
                String.valueOf(
                        attr.getLowAttribute(
                                rivenAnalyseTrend.getShotgun(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
        model.setHighAttr(
                String.valueOf(
                        attr.getHighAttribute(
                                rivenAnalyseTrend.getShotgun(),
                                v,
                                i,
                                attr.getNag(),
                                i >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())
                                        || RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                                        : RivenMatcherUtil.isNegativeAttribute(attr.getAttributeName(), attr.getAttribute())
                        )
                )
        );
    }

    private static void setRivenAnalyseTrend(RivenAnalyseTrendCompute.Attribute attr, RivenAnalyseTrendModel.Attribute model, double v, RivenAnalyseTrend rivenAnalyseTrend, int i) {

    }
}
