package com.nyx.bot.modules.warframe.utils.riven_calculation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.utils.DoubleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 紫卡趋势数据生成器
 * <p>
 * 从 DE 官方导出文件 ExportUpgrades.json 中截取紫卡基础数据，
 * 自动计算出 riven_analyse_trend 值。
 * </p>
 * <p>
 * 计算公式：trend = DE系数 × 9000（百分比类）或 × 90（非百分比类）
 * </p>
 */
@Slf4j
@Component
public class RivenTrendGenerator {

    private static final double PCT_MULTIPLIER = 9000.0;
    private static final double NON_PCT_MULTIPLIER = 90.0;

    /**
     * 非百分比类词条名称（使用 ×90 倍数）
     */
    private static final Set<String> NON_PCT_STATS = Set.of(
            "穿透", "连击持续时间", "攻击范围", "初始连击"
    );

    /**
     * DE 标签 → 趋势词条名称 映射
     */
    private static final Map<String, String> TAG_TO_TREND = new LinkedHashMap<>();

    static {
        TAG_TO_TREND.put("WeaponDamageAmountMod", "伤害/近战伤害");
        TAG_TO_TREND.put("WeaponMeleeDamageMod", "伤害/近战伤害");
        TAG_TO_TREND.put("WeaponCritChanceMod", "暴击几率");
        TAG_TO_TREND.put("WeaponCritDamageMod", "暴击伤害");
        TAG_TO_TREND.put("WeaponFireRateMod", "射速/攻击速度");
        TAG_TO_TREND.put("WeaponFireIterationsMod", "多重射击");
        TAG_TO_TREND.put("WeaponProcTimeMod", "触发时间");
        TAG_TO_TREND.put("WeaponStunChanceMod", "触发几率");
        TAG_TO_TREND.put("WeaponElectricityDamageMod", "电击伤害");
        TAG_TO_TREND.put("WeaponFireDamageMod", "火焰伤害");
        TAG_TO_TREND.put("WeaponFreezeDamageMod", "冰冻伤害");
        TAG_TO_TREND.put("WeaponToxinDamageMod", "毒素伤害");
        TAG_TO_TREND.put("WeaponImpactDamageMod", "冲击伤害");
        TAG_TO_TREND.put("WeaponSlashDamageMod", "切割伤害");
        TAG_TO_TREND.put("WeaponArmorPiercingDamageMod", "穿刺伤害");
        TAG_TO_TREND.put("WeaponAmmoMaxMod", "弹药最大值");
        TAG_TO_TREND.put("WeaponClipMaxMod", "弹匣容量");
        TAG_TO_TREND.put("WeaponRecoilReductionMod", "后坐力");
        TAG_TO_TREND.put("WeaponReloadSpeedMod", "装填速度");
        TAG_TO_TREND.put("WeaponProjectileSpeedMod", "投射物飞行速度");
        TAG_TO_TREND.put("WeaponPunctureDepthMod", "穿透");
        TAG_TO_TREND.put("WeaponZoomFovMod", "变焦");
        TAG_TO_TREND.put("WeaponFactionDamageCorpus", "对Corpus伤害");
        TAG_TO_TREND.put("WeaponFactionDamageGrineer", "对Grineer伤害");
        TAG_TO_TREND.put("WeaponFactionDamageInfested", "对Infested伤害");
        TAG_TO_TREND.put("WeaponMeleeFactionDamageCorpus", "对Corpus伤害");
        TAG_TO_TREND.put("WeaponMeleeFactionDamageGrineer", "对Grineer伤害");
        TAG_TO_TREND.put("WeaponMeleeFactionDamageInfested", "对Infested伤害");
        TAG_TO_TREND.put("WeaponMeleeRangeIncMod", "攻击范围");
        TAG_TO_TREND.put("WeaponMeleeFinisherDamageMod", "处决伤害");
        TAG_TO_TREND.put("WeaponMeleeComboEfficiencyMod", "重击效率");
        TAG_TO_TREND.put("WeaponMeleeComboInitialBonusMod", "初始连击");
        TAG_TO_TREND.put("WeaponMeleeComboPointsOnHitMod", "额外连击数几率");
        TAG_TO_TREND.put("WeaponMeleeComboBonusOnHitMod", "几率不获得连击数");
        TAG_TO_TREND.put("ComboDurationMod", "连击持续时间");
        TAG_TO_TREND.put("SlideAttackCritChanceMod", "滑行攻击暴击几率");
    }

    private final ObjectMapper objectMapper;

    public RivenTrendGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static double format(double coeff, double multiplier) {
        if (coeff == 0.0) return 0.0;
        return DoubleUtils.formatDouble4(coeff * multiplier);
    }

    /**
     * 从 JSON 节点提取首字母大写的字符串
     */
    private static String capitalize(JsonNode node, String field) {
        if (node == null || !node.has(field)) return "-";
        String val = node.get(field).asText();
        if (val == null || val.isEmpty()) return "-";
        return val.substring(0, 1).toUpperCase() + val.substring(1);
    }

    /**
     * 确定该 JSON 节点是否为紫卡基础数值表（而非紫卡Mod物品、星舰/玄骸脏数据）
     */
    private static boolean isRivenStatTable(JsonNode node) {
        if (node == null) return false;
        String uniqueName = node.has("uniqueName") ? node.get("uniqueName").asText() : "";
        String rarity = node.has("rarity") ? node.get("rarity").asText() : "";
        // 紫卡基础数值表: rarity=COMMON, 路径含 /Mods/Randomized/
        return "COMMON".equals(rarity) && uniqueName.contains("/Mods/Randomized/");
    }

    /**
     * 根据 uniqueName 识别武器类型（区分子类型，以便检测未来 DE 的系数分化）
     */
    private static WeaponCategory detectCategory(String uniqueName) {
        if (uniqueName.contains("Archgun")) return WeaponCategory.ARCHWING;
        if (uniqueName.contains("ModularMelee")) return WeaponCategory.ZAW;
        if (uniqueName.contains("PlayerMelee")) return WeaponCategory.MELEE;
        if (uniqueName.contains("ModularPistol")) return WeaponCategory.KITGUN;
        if (uniqueName.contains("LotusPistol")) return WeaponCategory.PISTOL;
        if (uniqueName.contains("Rifle")) return WeaponCategory.RIFLE;
        if (uniqueName.contains("Shotgun")) return WeaponCategory.SHOTGUN;
        return null;
    }

    /**
     * 将 entry 的 upgradeEntries 数组转为过滤后的 Stream，排除空 tag/空 upgradeValues。
     */
    private static Stream<TagEntry> streamUpgradeEntries(RivenEntry entry) {
        JsonNode upgradeEntries = entry.node.get("upgradeEntries");
        if (upgradeEntries == null || !upgradeEntries.isArray()) return Stream.empty();
        return StreamSupport.stream(upgradeEntries.spliterator(), false)
                .filter(ue -> ue.has("tag"))
                .filter(ue -> {
                    JsonNode uv = ue.get("upgradeValues");
                    return uv != null && uv.isArray() && !uv.isEmpty();
                })
                .filter(ue -> TAG_TO_TREND.containsKey(ue.get("tag").asText()))
                .map(ue -> new TagEntry(
                        entry.category,
                        ue,
                        ue.get("tag").asText(),
                        ue.get("upgradeValues").get(0).get("value").asDouble()));
    }

    /**
     * 从 DE 导出文件生成紫卡分析趋势数据
     *
     * @return 紫卡分析趋势列表
     */
    public List<RivenAnalyseTrend> generate() {
        String exportPath = ExportFilePath.resolve("ExportUpgrades");
        log.info("开始从 DE 导出文件生成紫卡趋势数据 [路径:{}]", exportPath);

        try {
            JsonNode root = objectMapper.readTree(new FileInputStream(exportPath));
            JsonNode upgrades = root.get("ExportUpgrades");
            if (upgrades == null || !upgrades.isArray()) {
                log.error("ExportUpgrades 数据为空或格式错误");
                return List.of();
            }

            // 筛选紫卡基础数值表：rarity=COMMON 且路径含 /Mods/Randomized/
            // 排除 Raw* 紫卡Mod物品（rarity=RARE，无系数）、CrewShip/KuvaLich 等脏数据
            List<RivenEntry> rivenEntries = StreamSupport.stream(upgrades.spliterator(), false)
                    .filter(RivenTrendGenerator::isRivenStatTable)
                    .map(node -> {
                        String uniqueName = node.get("uniqueName").asText();
                        WeaponCategory cat = detectCategory(uniqueName);
                        if (cat == null) {
                            log.debug("未识别的紫卡武器类型: {}", uniqueName);
                        }
                        return cat != null ? new RivenEntry(cat, node) : null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            log.info("筛选到 {} 条紫卡基础数据", rivenEntries.size());

            // 按趋势词条名称聚合：每个词条 → 各武器类型的最大系数
            return buildTrends(rivenEntries);
        } catch (Exception e) {
            log.error("生成紫卡趋势数据失败", e);
            return List.of();
        }
    }

    /**
     * 聚合计算趋势值
     */
    private List<RivenAnalyseTrend> buildTrends(List<RivenEntry> entries) {
        // trendName → weaponCategory → maxCoefficient
        Map<String, Map<WeaponCategory, Double>> coefficientMap = new LinkedHashMap<>();
        // trendName → prefix/suffix
        Map<String, String> prefixMap = new LinkedHashMap<>();
        Map<String, String> suffixMap = new LinkedHashMap<>();

        entries.stream()
                .flatMap(RivenTrendGenerator::streamUpgradeEntries)
                .forEach(te -> {
                    String trendName = TAG_TO_TREND.get(te.tag());
                    coefficientMap
                            .computeIfAbsent(trendName, k -> new EnumMap<>(WeaponCategory.class))
                            .merge(te.category(), Math.abs(te.coeff()), Math::max);
                    prefixMap.computeIfAbsent(trendName, k -> capitalize(te.ue(), "prefixTag"));
                    suffixMap.computeIfAbsent(trendName, k -> capitalize(te.ue(), "suffixTag"));
                });

        // 检测子类型分歧并合并
        warnIfDivergent(coefficientMap, WeaponCategory.MELEE, WeaponCategory.ZAW, "近战", "Zaw");
        warnIfDivergent(coefficientMap, WeaponCategory.PISTOL, WeaponCategory.KITGUN, "手枪", "Kitgun");

        // 构建 RivenAnalyseTrend 列表
        List<RivenAnalyseTrend> trends = coefficientMap.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    Map<WeaponCategory, Double> catMap = entry.getValue();
                    double multiplier = NON_PCT_STATS.contains(name) ? NON_PCT_MULTIPLIER : PCT_MULTIPLIER;

                    RivenAnalyseTrend trend = new RivenAnalyseTrend();
                    trend.setName(name);
                    trend.setPrefix(prefixMap.getOrDefault(name, "-"));
                    trend.setSuffix(suffixMap.getOrDefault(name, "-"));
                    trend.setRifle(format(catMap.getOrDefault(WeaponCategory.RIFLE, 0.0), multiplier));
                    trend.setShotgun(format(catMap.getOrDefault(WeaponCategory.SHOTGUN, 0.0), multiplier));
                    trend.setPistol(format(
                            Math.max(catMap.getOrDefault(WeaponCategory.PISTOL, 0.0),
                                    catMap.getOrDefault(WeaponCategory.KITGUN, 0.0)),
                            multiplier));
                    trend.setMelle(format(
                            Math.max(catMap.getOrDefault(WeaponCategory.MELEE, 0.0),
                                    catMap.getOrDefault(WeaponCategory.ZAW, 0.0)),
                            multiplier));
                    trend.setArchwing(format(catMap.getOrDefault(WeaponCategory.ARCHWING, 0.0), multiplier));
                    return trend;
                })
                .toList();

        log.info("生成 {} 条紫卡趋势数据", trends.size());
        return trends;
    }

    /**
     * 检测两个子类型的系数是否有分歧，有则发出告警
     */
    private void warnIfDivergent(Map<String, Map<WeaponCategory, Double>> coefficientMap,
                                 WeaponCategory cat1, WeaponCategory cat2,
                                 String cat1Name, String cat2Name) {
        for (var entry : coefficientMap.entrySet()) {
            String trendName = entry.getKey();
            Map<WeaponCategory, Double> catMap = entry.getValue();
            Double v1 = catMap.get(cat1);
            Double v2 = catMap.get(cat2);
            if (v1 != null && v2 != null && Math.abs(v1 - v2) > 1e-10) {
                log.warn("子类型系数分歧 [词条:{}] [{}系数:{}] [{}系数:{}] — 当前取最大值，"
                                + "建议在 RivenAnalyseTrend 中增加 {} / {} 独立字段",
                        trendName, cat1Name, v1, cat2Name, v2, cat1Name, cat2Name);
            }
        }
    }

    /**
     * 紫卡基础数据条目
     */
    /**
     * 武器类别枚举
     * <p>
     * MELEE/ZAW 最终合并写入 RivenAnalyseTrend.melle，
     * PISTOL/KITGUN 最终合并写入 RivenAnalyseTrend.pistol。
     * 若子类型系数出现分歧，将发出 WARNING 日志提醒扩展 schema。
     * </p>
     */
    private enum WeaponCategory {
        RIFLE, SHOTGUN, PISTOL, KITGUN, MELEE, ZAW, ARCHWING
    }

    private record TagEntry(WeaponCategory category, JsonNode ue, String tag, double coeff) {
    }

    private record RivenEntry(WeaponCategory category, JsonNode node) {
    }
}
