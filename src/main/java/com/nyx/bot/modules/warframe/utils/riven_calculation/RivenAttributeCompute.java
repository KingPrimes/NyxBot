package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.utils.OCRImage;
import com.nyx.bot.utils.RivenMatcherUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RivenAttributeCompute {


    public static List<List<RivenAnalyseTrendModel>> ocrRivenCompute(AnyMessageEvent event) {
        //识别图片
        List<List<String>> ocrImages = OCRImage.ocrImage(event);
        log.debug("识别文字：{}\n", ocrImages);
        //处理识别数据
        List<RivenAnalyseTrendCompute> riven = getRiven(ocrImages);
        log.debug("处理后得数据:{}\n", riven);
        //计算紫卡加成属性
        //转换成Json格式数据
        return setAttributeNumber(riven);
    }

    /**
     * 处理识别数据
     */
    public static List<RivenAnalyseTrendCompute> getRiven(List<List<String>> images) {
        List<RivenAnalyseTrendCompute> trends = new ArrayList<>();
        for (List<String> image : images) {
            //紫卡类
            RivenAnalyseTrendCompute trend = new RivenAnalyseTrendCompute();
            // 紫卡词条集合
            List<String> attributes = new ArrayList<>();
            // 获取紫卡属性
            for (String s : image) {
                if (s.length() < 3) {
                    continue;
                }
                // 武器名称
                if (RivenMatcherUtil.isWeaponsName(s)) {
                    if (s.contains("&")) {
                        int i = s.indexOf("&");
                        String star = s.substring(i, i + 1);
                        String end = s.substring(i - 1, i);
                        if (!star.contains(" ") && !end.contains(" ")) {
                            s = s.replace("&", " & ");
                        }
                    }
                    trend.setWeaponsName(RivenMatcherUtil.getChines(s));
                    if (RivenMatcherUtil.isRivenNameEx(s)) {
                        trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                    }
                }
                // 紫卡名称
                if (RivenMatcherUtil.isRivenNameEx(s)) {
                    if (trend.getRivenName() == null) {
                        trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                    } else {
                        if (!trend.getRivenName().equals(RivenMatcherUtil.getRivenNameE(s))) {
                            trend.setRivenName(trend.getRivenName() + RivenMatcherUtil.getRivenNameE(s));
                        }
                    }
                }
                // 紫卡词条
                if (RivenMatcherUtil.isAttribute(s)) {
                    s = s.replace(" ", "").trim();
                    if (s.contains("入")) {
                        s = s.replace("入", "");
                    }
                    attributes.add(s);
                }
            }
            boolean nag = false;
            // 判断紫卡词条是否大于等于3条
            if (attributes.size() >= 3) {
                String s = attributes.get(attributes.size() - 1);
                if (RivenMatcherUtil.getAttributeNum(s) < 0 || RivenMatcherUtil.whetherItIsDiscrimination(s)) {
                    nag = true;
                }
            }
            // 设置具体的词条属性
            for (String s : attributes) {
                RivenAnalyseTrendCompute.Attribute attribute = new RivenAnalyseTrendCompute.Attribute();
                if (s.contains("射速")) {
                    attribute.setAttributeName(s + " 效果加倍）");
                    attribute.setName(RivenMatcherUtil.getAttributeName(s) + " 效果加倍）");
                } else if (s.contains("暴击几率（")) {
                    attribute.setAttributeName(s.replaceAll("（重?击?时?", "(").trim() + "重击时 x2)");
                    attribute.setName("暴击几率（重击时 x2）");
                } else if (s.contains("滑行攻击")) {
                    attribute.setAttributeName(s.replaceAll("滑.*+", "滑行攻击暴击几率"));
                    attribute.setName("滑行攻击暴击几率");
                } else {
                    attribute.setAttributeName(s);
                    attribute.setName(RivenMatcherUtil.getAttributeName(s));
                }
                attribute.setAttribute(RivenMatcherUtil.getAttributeNum(s));
                /* attribute.setNag(MatchUtil.getAttributeNum(s) < 0)*//*|| (MatchUtil.whetherItIsDiscrimination(s) && MatchUtil.getAttributeNum(s) > 0))*//*;*/
                // 用于判断是否携带负属性
                attribute.setNag(nag);
                trend.add(attribute);
            }
            trends.add(trend);
        }
        return trends;
    }

    /**
     * 计算紫卡加成数据
     */
    public static List<List<RivenAnalyseTrendModel>> setAttributeNumber(List<RivenAnalyseTrendCompute> trends) {
        //存放多张紫卡
        List<List<RivenAnalyseTrendModel>> rives = new ArrayList<>();
        List<RivenAnalyseTrendModel> models = new ArrayList<>();
        RivenLookup lookup = new RivenLookup();
        //遍历识别处理后的数据
        for (RivenAnalyseTrendCompute trend : trends) {
            List<Weapons> weapons = lookup.findByFuzzyName(trend.getWeaponsName());
            for (Weapons weapon : weapons) {
                Double omegaAttenuation = weapon.getOmegaAttenuation();
                RivenAnalyseTrendModel model = new RivenAnalyseTrendModel();
                model.setWeaponName(weapon.getName());
                model.setRivenName(trend.getRivenName());
                model.setNewNum(omegaAttenuation);
                model.setNewDot(RivenTrendEnum.getRivenTrendDot(omegaAttenuation));
                model.setWeaponType(weapon.getProductCategory().getName());
                if (weapon.getProductCategory().equals(Weapons.ProductCategory.SentinelWeapons)) {
                    if (weapon.getDescription().contains("霰弹枪")) {
                        weapon.setProductCategory(Weapons.ProductCategory.Shotguns);
                    } else if (weapon.getDescription().contains("近战") || weapon.getName().contains("分离")) {
                        weapon.setProductCategory(Weapons.ProductCategory.Melee);
                    } else {
                        weapon.setProductCategory(Weapons.ProductCategory.LongGuns);
                    }
                } else if (weapon.getDescription().contains("霰弹枪") || weapon.getDescription().contains("散弹枪")) {
                    weapon.setProductCategory(Weapons.ProductCategory.Shotguns);
                } else {
                    model.setWeaponType(weapon.getProductCategory().getName());
                }
                List<RivenAnalyseTrendModel.Attribute> attrs = new ArrayList<>();
                for (int idx = 0; idx < trend.getAttributes().size(); idx++) {
                    var attr = trend.getAttributes().get(idx);
                    var analyse = lookup.findRivenTrendByAnalyseName(attr.getName()).orElse(new RivenAnalyseTrend());
                    var m = new RivenAnalyseTrendModel.Attribute();
                    m.setAttributeName(attr.getAttributeName());
                    m.setAttr(attr.getAttribute());
                    m.setName(attr.getName());
                    AttributeCalculator.CALCULATORS
                            .get(weapon.getProductCategory())
                            .calculate(attr, m, omegaAttenuation, analyse, trend.getAttributes().size());
                    m.setAttrDiff(attrDiff(attr));
                    attrs.add(m);
                }
                model.setAttributes(attrs);
                models.add(model);
            }
            rives.add(models);
        }
        return rives;
    }

    private static String attrDiff(RivenAnalyseTrendCompute.Attribute attribute) {
        String name = attribute.getName();
        if (name.contains("Infested") || name.contains("Corpus") || name.contains("Grinner") || name.contains("Grineer")) {
            return attribute.getAttributeDiscriminationDiff();
        } else {
            return attribute.getAttributeDiff();
        }
    }
}
