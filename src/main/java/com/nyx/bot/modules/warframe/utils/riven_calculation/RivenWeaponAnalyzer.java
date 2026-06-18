package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons.ProductCategory;
import com.nyx.bot.modules.warframe.utils.RivenMatcherUtil;
import io.github.kingprimes.model.RivenAnalyseTrendModel;
import org.springframework.stereotype.Component;

/**
 * 紫卡综合分析器
 * <p>
 * 结合武器基础属性，对紫卡词条进行综合分析评估：
 * <ul>
 *   <li>比率 — 紫卡值与该武器类型现有MOD的比值</li>
 *   <li>评分 — S/A/B/C/D 综合价值等级</li>
 *   <li>致命度 — 负属性对当前武器的致命程度</li>
 *   <li>分析 — 综合评语</li>
 * </ul>
 * </p>
 */
@Component
public class RivenWeaponAnalyzer {

    // ══════════════════════════════════════════════
    // 评分阈值常量
    // ══════════════════════════════════════════════
    /**
     * 低暴击阈值 — 低于此值暴击词条价值有限 (15%)
     */
    private static final double LOW_CRIT = 0.15;
    /**
     * 高暴击阈值 — 高于此值暴击词条价值显著 (18%)
     */
    private static final double HIGH_CRIT = 0.18;
    /**
     * 暴击致命阈值 — 高于此值负暴击严重 (25%)
     */
    private static final double CRIT_FATAL = 0.25;
    /**
     * 高触发阈值 — 高于此值触发词条价值显著 (20%)
     */
    private static final double HIGH_PROC = 0.20;
    /**
     * 触发致命阈值 — 高于此值的触发霰弹负触发致命 (25%)
     */
    private static final double PROC_FATAL = 0.25;
    /**
     * 霰弹枪无暴击时基伤价值更高 (10%)
     */
    private static final double LOW_CRIT_FALLBACK = 0.10;
    /**
     * 高射速武器阈值（发/秒）
     */
    private static final double HIGH_FIRE_RATE = 5.0;
    /**
     * 低弹匣致命阈值
     */
    private static final int LOW_MAG_FATAL = 10;

    // ══════════════════════════════════════════════
    // 主入口
    // ══════════════════════════════════════════════

    private static boolean isSniper(Weapons weapon) {
        String desc = weapon.getDescription();
        return desc != null && desc.contains("狙击");
    }

    // ══════════════════════════════════════════════
    // 比率计算 — 基于武器的数据库基值
    // ══════════════════════════════════════════════

    /**
     * 对紫卡的所有词条进行综合分析
     *
     * @param weapon 武器数据
     * @param model  已计算偏差的紫卡模型，直接写入分析字段
     */
    public void analyze(Weapons weapon, RivenAnalyseTrendModel model) {
        if (weapon == null || model == null || model.getAttributes() == null) return;

        ProductCategory category = weapon.getEffectiveCategory();
        double baseCrit = weapon.getCriticalChance() != null ? weapon.getCriticalChance() : 0.0;
        double baseProc = weapon.getProcChance() != null ? weapon.getProcChance() : 0.0;
        boolean isSniper = isSniper(weapon);

        for (var attr : model.getAttributes()) {
            String name = attr.getName();
            double attrVal = attr.getAttr() != null ? attr.getAttr() : 0.0;
            boolean isNegative = RivenMatcherUtil.isInvertedAttribute(name)
                    ? attrVal > 0   // 后坐力等：正值为坏(负属性)，负值为好(正属性)
                    : attrVal < 0;

            attr.setRatio(calcRatio(weapon, name, Math.abs(attrVal)));
            attr.setGrade(calcGrade(category, name, baseCrit, baseProc, isSniper));

            // 3. 致命度（仅负属性）
            if (isNegative) {
                attr.setLethalLevel(calcLethalLevel(category, name, weapon));
            }

            // 4. 综合分析文本
            attr.setAnalysis(buildAnalysis(attr));
        }
    }

    // ══════════════════════════════════════════════
    // 评分 (S/A/B/C/D)
    // ══════════════════════════════════════════════

    /**
     * 计算紫卡词条值相对于武器基值的倍率。
     * <p>所有数据均来自 Weapons 实体，无双写死 MOD 值。</p>
     */
    private String calcRatio(Weapons weapon, String trendName, double rivenValue) {
        double baseRef = switch (trendName) {
            case "暴击几率" -> weapon.getCriticalChance() != null ? weapon.getCriticalChance() * 100 : 0;
            case "暴击伤害" -> weapon.getCriticalMultiplier() != null ? (weapon.getCriticalMultiplier() - 1) * 100 : 0;
            case "触发几率" -> weapon.getProcChance() != null ? weapon.getProcChance() * 100 : 0;
            case "射速/攻击速度" -> weapon.getFireRate() != null ? weapon.getFireRate() * 100 : 0;
            case "伤害/近战伤害" -> weapon.getTotalDamage() != null ? weapon.getTotalDamage() : 0;
            case "弹匣容量" -> weapon.getMagazineSize() != null ? weapon.getMagazineSize() * 100 : 0;
            case "装填速度" -> weapon.getReloadTime() != null ? weapon.getReloadTime() * 100 : 0;
            default -> 0;
        };
        if (baseRef <= 0) return "-";
        return String.format("%.2f", rivenValue / baseRef);
    }

    private String calcGrade(ProductCategory category, String trendName,
                             double baseCrit, double baseProc, boolean isSniper) {
        return switch (category) {
            case Shotguns -> shotgunGrade(trendName, baseCrit, baseProc);
            case Pistols -> pistolGrade(trendName, baseCrit);
            case Melee -> meleeGrade(trendName, baseCrit);
            case LongGuns -> rifleGrade(trendName, baseCrit, isSniper);
            default -> "C";
        };
    }

    // -- 霰弹枪 --
    private String shotgunGrade(String name, double baseCrit, double baseProc) {
        return switch (name) {
            case "伤害/近战伤害" -> baseCrit > LOW_CRIT_FALLBACK ? "B" : "A";
            case "多重射击" -> "S";
            case "暴击几率", "暴击伤害" -> baseCrit > LOW_CRIT ? "A" : "C";
            case "触发几率", "切割伤害" -> baseProc > HIGH_PROC ? "A" : "B";
            case "射速/攻击速度" -> "B";
            case "冰冻伤害", "毒素伤害", "电击伤害", "火焰伤害" -> baseCrit > LOW_CRIT ? "B" : "A";
            case "穿刺伤害", "冲击伤害", "装填速度", "投射物飞行速度" -> "C";
            default -> "-";
        };
    }

    // -- 手枪 --
    private String pistolGrade(String name, double baseCrit) {
        return switch (name) {
            case "伤害/近战伤害" -> "S";
            case "多重射击" -> baseCrit > LOW_CRIT ? "B" : "A";
            case "暴击几率", "暴击伤害" -> baseCrit > HIGH_CRIT ? "A" : "B";
            case "触发几率", "冰冻伤害", "毒素伤害", "电击伤害", "穿刺伤害", "冲击伤害", "切割伤害" -> "B";
            case "射速/攻击速度", "装填速度" -> "C";
            case "火焰伤害" -> baseCrit > HIGH_CRIT ? "B" : "A";
            default -> "-";
        };
    }

    // -- 近战 --
    private String meleeGrade(String name, double baseCrit) {
        return switch (name) {
            case "伤害/近战伤害" -> "B";
            case "暴击几率" -> "C";
            case "暴击伤害" -> "S";
            case "触发几率", "射速/攻击速度" -> "B";
            case "攻击范围", "初始连击", "切割伤害" -> "A";
            case "连击持续时间" -> baseCrit > LOW_CRIT ? "A" : "B";
            case "冰冻伤害", "毒素伤害", "电击伤害", "火焰伤害" -> baseCrit > HIGH_CRIT ? "B" : "A";
            case "处决伤害", "重击效率", "冲击伤害", "穿刺伤害" -> "C";
            case "滑行攻击暴击几率" -> "D";
            default -> "-";
        };
    }

    // ══════════════════════════════════════════════
    // 负属性致命度
    // ══════════════════════════════════════════════

    // -- 步枪 --
    private String rifleGrade(String name, double baseCrit, boolean isSniper) {
        return switch (name) {
            case "伤害/近战伤害", "触发几率", "冰冻伤害", "毒素伤害", "电击伤害", "火焰伤害", "穿刺伤害", "冲击伤害" ->
                    "B";
            case "多重射击" -> isSniper ? "S" : "A";
            case "暴击几率", "暴击伤害" -> baseCrit > HIGH_CRIT ? "S" : "A";
            case "射速/攻击速度", "装填速度", "弹匣容量" -> "C";
            case "切割伤害" -> baseCrit > HIGH_CRIT ? "B" : "A";
            default -> "-";
        };
    }

    /**
     * 致命度等级
     * <ul>
     *   <li>fatal — 直接废卡</li>
     *   <li>serious — 严重影响</li>
     *   <li>harmful — 有负面效果</li>
     *   <li>acceptable — 影响不大</li>
     *   <li>beneficial — 负面实为正面</li>
     * </ul>
     */
    private String calcLethalLevel(ProductCategory category, String trendName, Weapons weapon) {
        // 检查是否是有益的"负面"
        if (isBeneficialNegative(category, trendName, weapon)) {
            return "beneficial";
        }

        double baseCrit = weapon.getCriticalChance() != null ? weapon.getCriticalChance() : 0.0;
        double baseProc = weapon.getProcChance() != null ? weapon.getProcChance() : 0.0;
        int magSize = weapon.getMagazineSize() != null ? weapon.getMagazineSize() : 999;

        return switch (trendName) {
            case "触发几率" -> (baseProc > PROC_FATAL && category == ProductCategory.Shotguns) ? "fatal" :
                    baseProc > HIGH_PROC ? "serious" : "harmful";
            case "连击持续时间" -> (category == ProductCategory.Melee && baseCrit > LOW_CRIT) ? "fatal" : "serious";
            case "初始连击", "多重射击" -> "serious";
            case "弹匣容量" -> magSize < LOW_MAG_FATAL ? "fatal" : "harmful";
            case "伤害/近战伤害" -> "harmful";
            case "暴击几率" -> baseCrit > CRIT_FATAL ? "serious" : "acceptable";
            case "暴击伤害" -> baseCrit > HIGH_CRIT ? "serious" : "acceptable";
            case "射速/攻击速度" ->
                    (weapon.getFireRate() != null && weapon.getFireRate() > HIGH_FIRE_RATE) ? "serious" : "harmful";
            case "攻击范围" -> category == ProductCategory.Melee ? "serious" : "harmful";
            case "后坐力" -> "harmful";
            case "穿刺伤害", "冲击伤害", "弹药最大值", "投射物飞行速度" -> "acceptable";
            default -> "harmful";
        };
    }

    /**
     * 检测负属性是否实际上是有益的
     */
    private boolean isBeneficialNegative(ProductCategory category, String trendName, Weapons weapon) {
        double slashRatio = getSlashRatio(weapon);
        return switch (trendName) {
            // 负冲击 — 提升切割占比，对主切割武器有益 (切割占比 > 50%)
            case "冲击伤害" -> slashRatio > 0.5;
            // 负穿刺 — 对主切割武器有益
            case "穿刺伤害" -> slashRatio > 0.5;
            // 负后坐力 — 对霰弹减小扩散
            case "后坐力" -> category == ProductCategory.Shotguns;
            // 负变焦 — 对狙击枪有益，更快达到开镜加成
            case "变焦" -> isSniper(weapon);
            default -> false;
        };
    }

    // ══════════════════════════════════════════════
    // 综合分析文本
    // ══════════════════════════════════════════════

    /**
     * 计算武器物理伤害中切割的占比 (0.0~1.0)
     */
    private double getSlashRatio(Weapons weapon) {
        var dps = weapon.getDamagePerShot();
        if (dps == null || dps.size() < 3) return 0.0;
        double impact = dps.get(0) != null ? dps.get(0) : 0.0;
        double puncture = dps.get(1) != null ? dps.get(1) : 0.0;
        double slash = dps.get(2) != null ? dps.get(2) : 0.0;
        double total = impact + puncture + slash;
        return total > 0 ? slash / total : 0.0;
    }

    private String buildAnalysis(RivenAnalyseTrendModel.Attribute attr) {
        StringBuilder sb = new StringBuilder();
        String grade = attr.getGrade();
        if (grade != null && !grade.equals("-")) {
            sb.append(gradeToText(grade));
        }
        String ratio = attr.getRatio();
        if (ratio != null && !ratio.equals("-")) {
            sb.append("加成/基值: ").append(ratio).append("倍；");
        }
        String lethal = attr.getLethalLevel();
        if (lethal != null) {
            sb.append(lethalToText(lethal));
        }
        return sb.toString();
    }

    private String gradeToText(String grade) {
        return switch (grade) {
            case "S" -> "对该武器价值极高；";
            case "A" -> "对该武器较为优秀；";
            case "B" -> "对该武器中规中矩；";
            case "C" -> "对该武器价值偏低；";
            case "D" -> "对该武器价值很低；";
            default -> "";
        };
    }

    // ══════════════════════════════════════════════
    // 武器特性检测
    // ══════════════════════════════════════════════

    private String lethalToText(String lethal) {
        return switch (lethal) {
            case "fatal" -> "负属性致命，直接废卡！";
            case "serious" -> "负属性严重影响输出/手感；";
            case "harmful" -> "负属性有一定负面影响；";
            case "acceptable" -> "该负属性对该武器影响不大；";
            case "beneficial" -> "该负属性反而对该武器有益！";
            default -> "";
        };
    }

    // ══════════════════════════════════════════════
    // 辅助方法
    // ══════════════════════════════════════════════

}
