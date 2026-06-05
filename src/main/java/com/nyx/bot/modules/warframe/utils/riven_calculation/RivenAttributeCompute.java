package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.utils.OCRImage;
import com.nyx.bot.modules.warframe.utils.RivenMatcherUtil;
import com.nyx.bot.utils.StringUtils;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import io.github.kingprimes.model.enums.RivenTrendEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RivenAttributeCompute {

    private final RivenLookup rivenLookup;
    private final RivenWeaponAnalyzer rivenWeaponAnalyzer;

    public RivenAttributeCompute(RivenLookup rivenLookup, RivenWeaponAnalyzer rivenWeaponAnalyzer) {
        this.rivenLookup = rivenLookup;
        this.rivenWeaponAnalyzer = rivenWeaponAnalyzer;
    }

    /**
     * 处理识别数据
     */
    public static RivenAnalyseTrendCompute getRiven(List<String> image) {
        // 先过滤 OCR 噪音，仅保留武器名、紫卡名、属性词条
        List<String> cleaned = RivenOcrFilter.clean(image);
        RivenAnalyseTrendCompute trend = new RivenAnalyseTrendCompute();
        List<String> attributes = new ArrayList<>();
        for (String s : cleaned) {
            if (s.length() < 3) {
                continue;
            }
            if (RivenMatcherUtil.isWeaponsName(s)) {
                if (s.contains("&") && s.indexOf("&") > 0) {
                    int i = s.indexOf("&");
                    String prev = s.substring(i - 1, i);
                    if (!prev.contains(" ")) {
                        s = s.replace("&", " & ");
                    }
                }
                trend.setWeaponsName(RivenMatcherUtil.getChines(s));
                if (RivenMatcherUtil.isRivenNameEx(s)) {
                    trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                }
            }
            if (RivenMatcherUtil.isRivenNameEx(s)) {
                if (trend.getRivenName() == null) {
                    trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                } else {
                    if (!trend.getRivenName().equals(RivenMatcherUtil.getRivenNameE(s))) {
                        trend.setRivenName(trend.getRivenName() + RivenMatcherUtil.getRivenNameE(s));
                    }
                }
            }
            if (RivenMatcherUtil.isAttribute(s)) {
                s = s.replace(" ", "").trim();
                if (s.contains("入")) {
                    s = s.replace("入", "");
                }
                attributes.add(s);
            }
        }
        // 判断紫卡是否携带负属性：3条以上词条时遍历全部，不可只看最后一条
        // 需同时检查反向属性（如后坐力正值为负属性）
        boolean nag = attributes.size() >= 3 && attributes.stream().anyMatch(s2 ->
                RivenMatcherUtil.getAttributeNum(s2) < 0
                        || RivenMatcherUtil.whetherItIsDiscrimination(s2)
                        || RivenMatcherUtil.isNegativeAttribute(
                        RivenMatcherUtil.getAttributeName(s2), RivenMatcherUtil.getAttributeNum(s2)));
        for (String s : attributes) {
            RivenAnalyseTrendCompute.Attribute attribute = new RivenAnalyseTrendCompute.Attribute();
            if (s.contains("射速")) {
                attribute.setAttributeName(s + " 效果加倍）");
                attribute.setName(RivenMatcherUtil.getAttributeName(s) + " 效果加倍）");
            } else if (s.contains("暴击几率（") || s.contains("重击")) {
                attribute.setAttributeName(StringUtils.substring(s, 0, !s.contains("率") ? s.length() : s.indexOf("率") + 1) + "(重击时 x2)");
                attribute.setName("暴击几率（重击时 x2）");
            } else if (s.contains("滑行攻击")) {
                attribute.setAttributeName(s.replaceAll("滑.*+", "滑行攻击暴击几率"));
                attribute.setName("滑行攻击暴击几率");
            } else {
                attribute.setAttributeName(s);
                attribute.setName(RivenMatcherUtil.getAttributeName(s));
            }
            attribute.setAttribute(RivenMatcherUtil.getAttributeNum(s));
            attribute.setNag(nag);
            trend.add(attribute);
        }
        return trend;
    }

    private static Double getTrendValue(Weapons.ProductCategory cat, RivenAnalyseTrend trend) {
        return switch (cat) {
            case LongGuns, SentinelWeapons -> trend.getRifle();
            case Shotguns -> trend.getShotgun();
            case Pistols -> trend.getPistol();
            case Melee -> trend.getMelle();
            case SpaceGuns, SpaceMelee -> trend.getArchwing();
            default -> null;
        };
    }

    /**
     * 紫卡词条修正系数（不含 grade 因子 0.9~1.1）
     */
    static double correctionFactor(int totalAttrs, boolean hasNegative, boolean isNegativeAttr) {
        return switch (totalAttrs) {
            case 2 -> isNegativeAttr ? 0 : 0.99;
            case 3 -> hasNegative
                    ? (isNegativeAttr ? -0.495 : 1.2375)
                    : (isNegativeAttr ? 0 : 0.75);
            case 4 -> isNegativeAttr ? -0.75 : 0.9375;
            default -> 1.0;
        };
    }

    private static String attrDiff(RivenAnalyseTrendCompute.Attribute attribute) {
        String name = attribute.getName();
        if (name.contains("Infested") || name.contains("Corpus") || name.contains("Grinner") || name.contains("Grineer")) {
            return attribute.getAttributeDiscriminationDiff();
        } else {
            return attribute.getAttributeDiff();
        }
    }

    public List<RivenAnalyseTrendModel> ocrRivenCompute(AnyMessageEvent event) {
        List<List<String>> ocrImages = OCRImage.ocrImage(event);
        log.debug("识别文字：{}\n", ocrImages);
        if (ocrImages.isEmpty()) {
            log.warn("OCR未识别到任何图片文字");
            return List.of();
        }
        List<RivenAnalyseTrendModel> allModels = new ArrayList<>();
        for (List<String> image : ocrImages) {
            List<String> cleaned = RivenOcrFilter.clean(image);
            log.debug("OCR 清洗前 {} 行 → 清洗后 {} 行: {}", image.size(), cleaned.size(), cleaned);
            RivenAnalyseTrendCompute riven = getRiven(cleaned);
            log.debug("处理后得数据:{}\n", riven);
            allModels.addAll(setAttributeNumber(riven));
        }
        return allModels;
    }

    /**
     * 计算紫卡加成数据
     */
    public List<RivenAnalyseTrendModel> setAttributeNumber(RivenAnalyseTrendCompute trend) {
        List<RivenAnalyseTrendModel> models = new ArrayList<>();
        List<Weapons> weapons = rivenLookup.findByFuzzyName(trend.getWeaponsName());
        if (weapons.isEmpty()) return models;

        // OCR 值反映紫卡当前所在武器的倾向，且可能非满级。
        // 先用来源武器估算紫卡等级，再统一缩放至满级值用于偏差对比。
        double sourceDisp = weapons.getFirst().getOmegaAttenuation();
        double rankScale = estimateRankScale(trend, weapons.getFirst());
        log.debug("Weapons:{}, sourceDisp:{}, rankScale:{}", weapons, sourceDisp, rankScale);

        for (Weapons weapon : weapons) {
            Double omegaAttenuation = weapon.getOmegaAttenuation();
            double dispScale = sourceDisp > 0 ? omegaAttenuation / sourceDisp : 1.0;
            // 满级等效值 = OCR值 × (满级9/(等级+1)) × (目标倾向/来源倾向)
            double maxEquivScale = rankScale * dispScale;
            RivenAnalyseTrendModel model = new RivenAnalyseTrendModel();
            model.setWeaponName(weapon.getName());
            model.setRivenName(trend.getRivenName());
            model.setNum(omegaAttenuation);
            model.setDot(RivenTrendEnum.getRivenTrendDot(omegaAttenuation));
            Weapons.ProductCategory effectiveCategory = weapon.getEffectiveCategory();
            model.setWeaponType(effectiveCategory.getName());
            List<RivenAnalyseTrendModel.Attribute> attrs = new ArrayList<>();
            for (int idx = 0; idx < trend.getAttributes().size(); idx++) {
                var attr = trend.getAttributes().get(idx);
                var analyse = rivenLookup.findRivenTrendByAnalyseName(attr.getName()).orElse(new RivenAnalyseTrend());
                var m = new RivenAnalyseTrendModel.Attribute();
                m.setAttributeName(attr.getAttributeName());
                // 满级等效值 = OCR值 × 满级缩放 × 倾向缩放
                // 歧视词条 x1.53 格式需特殊处理：内部百分比线性缩放，显示值非线性
                double scaledAttr;
                if (RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())) {
                    double ocr = attr.getAttribute();
                    double pct = ocr > 1 ? (ocr - 1) * 100 : ocr * 100 - 100;
                    scaledAttr = 1 + (pct * maxEquivScale) / 100;
                } else {
                    scaledAttr = attr.getAttribute() * maxEquivScale;
                }
                m.setAttr(scaledAttr);
                m.setName(attr.getName());
                double originalAttr = attr.getAttribute();
                attr.setAttribute(scaledAttr);
                AttributeCalculator.CALCULATORS
                        .get(effectiveCategory)
                        .calculate(attr, m, omegaAttenuation, analyse, trend.getAttributes().size());
                m.setAttrDiff(attrDiff(attr));
                attr.setAttribute(originalAttr);
                attrs.add(m);
            }
            model.setAttributes(attrs);
            rivenWeaponAnalyzer.analyze(weapon, model);
            models.add(model);
        }
        return models;
    }

    /**
     * 通过 OCR 值与预期满级中位数的比值估算紫卡当前等级，
     * 返回缩放至满级 (rank 8) 的倍率 = 9 / (estimatedRank + 1)。
     */
    private double estimateRankScale(RivenAnalyseTrendCompute trend, Weapons sourceWeapon) {
        double sourceDisp = sourceWeapon.getOmegaAttenuation();
        Weapons.ProductCategory cat = sourceWeapon.getEffectiveCategory();
        int n = trend.getAttributes().size();
        boolean nag = trend.getAttributes().stream().anyMatch(a -> Boolean.TRUE.equals(a.getNag()));

        double sumRatio = 0;
        int count = 0;
        for (var attr : trend.getAttributes()) {
            var trendEntry = rivenLookup.findRivenTrendByAnalyseName(attr.getName()).orElse(null);
            if (trendEntry == null) continue;
            Double baseVal = getTrendValue(cat, trendEntry);
            if (baseVal == null || baseVal == 0) continue;

            boolean isNegative = RivenMatcherUtil.isNegativeAttribute(attr.getName(), attr.getAttribute());
            double correction = correctionFactor(n, nag, isNegative);
            double expectedMedian = Math.abs(baseVal) * sourceDisp * Math.abs(correction);
            if (expectedMedian <= 0) continue;

            // 歧视词条 OCR 值为 x1.53 格式，需转为百分比 (53%) 再与 trend 值 (45.0) 比较
            double normalizedVal = Math.abs(attr.getAttribute());
            if (RivenMatcherUtil.whetherItIsDiscrimination(attr.getAttributeName())) {
                normalizedVal = normalizedVal > 1 ? (normalizedVal - 1) * 100 : 100 - (normalizedVal * 100);
            }
            double ratio = normalizedVal / expectedMedian;
            sumRatio += ratio;
            count++;
        }

        if (count == 0) return 1.0;
        double avgRatio = sumRatio / count;
        // ratio = gradeFactor × (rank+1)/9, gradeFactor ∈ [0.9, 1.1]
        // 估算: (rank+1)/9 ≈ avgRatio / 1.0（取 grade=1.0 为中间值）
        int estimatedRank = (int) Math.round(avgRatio * 9 - 1);
        estimatedRank = Math.clamp(estimatedRank, 0, 8);
        double rankScale = 9.0 / (estimatedRank + 1);
        log.debug("紫卡等级估算: avgRatio={} → rank={}, scaleToMax={}", avgRatio, estimatedRank, rankScale);
        if (rankScale > 2.0) {
            log.warn("紫卡等级估算异常 (scale={})，OCR数据可能存在噪音或非满级紫卡", rankScale);
        }
        return rankScale;
    }
}
