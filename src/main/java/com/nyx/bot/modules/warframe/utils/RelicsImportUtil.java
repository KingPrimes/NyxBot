package com.nyx.bot.modules.warframe.utils;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.entity.exprot.RelicRewards;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.RelicsRepository;
import com.nyx.bot.utils.ListUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Relics数据导入工具类
 * 封装数据读取、翻译处理和批量插入功能
 */
public class RelicsImportUtil {
    private static final Logger log = LoggerFactory.getLogger(RelicsImportUtil.class);
    private static final int BATCH_SIZE = 500;

    // 用于收集未翻译的奖励名称
    private final List<Map<String, String>> untranslatedItems = new ArrayList<>();

    private final StateTranslationRepository stateTranslationRepository;
    private final RelicsRepository relicsRepository;

    // 构造器注入依赖
    public RelicsImportUtil(StateTranslationRepository stateTranslationRepo,
                            RelicsRepository relicsRepo) {
        this.stateTranslationRepository = stateTranslationRepo;
        this.relicsRepository = relicsRepo;
    }

    /**
     * 导入Relics数据的主方法
     *
     * @param filePath 数据文件路径
     * @return 导入成功的记录数
     */
    @Transactional
    public int importRelicsData(String filePath) {
        try {
            // 1. 读取并解析JSON数据
            List<Relics> originalRelics = readRelicsFromFile(filePath);
            if (originalRelics.isEmpty()) {
                log.warn("未解析到任何Relics数据");
                return 0;
            }
            // 新增：根据name字段去重 (保留第一个出现的记录)
            List<Relics> distinctRelics = originalRelics.stream()
                    .collect(Collectors.toMap(
                            Relics::getName,       // 以name为key
                            Function.identity(),   // value为对象本身
                            (existing, replacement) -> existing  // 重复时保留第一个
                    ))
                    .values()
                    .stream()
                    .toList();
            // 2. 预处理数据（过滤+提取翻译关键词）
            List<Relics> filteredRelics = filterSecretRelics(distinctRelics);
            List<String> rewardKeywords = extractRewardKeywords(filteredRelics);

            // 3. 预加载翻译数据
            Map<String, String> translationMap = loadTranslationMap(rewardKeywords);

            // 4. 翻译处理
            List<Relics> processedRelics = processRelicsTranslation(filteredRelics, translationMap);

            // 5. 导出未翻译数据
            exportUntranslatedItems();

            // 6. 批量插入数据库
            batchInsertRelics(processedRelics);

            log.info("数据导入完成，共处理{}条有效记录", processedRelics.size());
            return processedRelics.size();

        } catch (Exception e) {
            log.error("数据导入失败", e);
            throw new RuntimeException("Relics数据导入失败", e);
        }
    }

    /**
     * 从文件读取Relics数据
     */
    private List<Relics> readRelicsFromFile(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            JSONArray jsonArray = JSON.parseObject(fis).getJSONArray("ExportRelicArcane");
            return jsonArray.toJavaList(Relics.class);
        } catch (FileNotFoundException e) {
            log.error("数据文件不存在: {}", filePath);
            throw e;
        } catch (JSONException e) {
            log.error("JSON数据解析失败", e);
            throw e;
        }
    }

    /**
     * 过滤掉机密遗物
     */
    private List<Relics> filterSecretRelics(List<Relics> relics) {
        return relics.stream()
                .filter(relic -> relic != null && Boolean.FALSE.equals(relic.getCodexSecret()))
                .collect(Collectors.toList());
    }

    /**
     * 提取需要翻译的关键词
     */
    private List<String> extractRewardKeywords(List<Relics> relics) {
        return relics.stream()
                .flatMap(this::extractRelicRewards)
                .map(reward -> StringUtils.getLastThreeSegments(reward.getRewardName()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 提取遗物奖励流（处理null安全）
     */
    private Stream<RelicRewards> extractRelicRewards(Relics relic) {
        List<RelicRewards> rewards = relic.getRelicRewards();
        return rewards != null ? rewards.stream() : Stream.empty();
    }

    /**
     * 加载翻译映射表
     */
    private Map<String, String> loadTranslationMap(List<String> keywords) {
        if (keywords.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用Specification动态查询
        List<StateTranslation> translations = stateTranslationRepository.findAll(
                buildTranslationSpecification(keywords)
        );

        // 构建关键词到翻译的映射
        return translations.stream()
                .collect(Collectors.toMap(
                        st -> findMatchingKeyword(st.getUniqueName(), keywords),
                        StateTranslation::getName,
                        (existing, replacement) -> existing // 保留第一个匹配项
                ));
    }

    /**
     * 构建翻译查询条件
     */
    private Specification<StateTranslation> buildTranslationSpecification(List<String> keywords) {
        return (root, query, cb) -> {
            Predicate[] predicates = keywords.stream()
                    .map(keyword -> cb.like(root.get("uniqueName"), "%" + keyword))
                    .toArray(Predicate[]::new);
            return cb.or(predicates);
        };
    }

    /**
     * 查找与uniqueName匹配的关键词
     */
    private String findMatchingKeyword(String uniqueName, List<String> keywords) {
        return keywords.stream()
                .filter(uniqueName::endsWith)
                .findFirst()
                .orElse(uniqueName); // 找不到时使用原始名称作为key
    }

    /**
     * 处理遗物翻译
     */
    private List<Relics> processRelicsTranslation(List<Relics> relics, Map<String, String> translationMap) {
        return relics.stream()
                .map(relic -> translateSingleRelic(relic, translationMap))
                .collect(Collectors.toList());
    }

    /**
     * 翻译单个遗物
     */
    private Relics translateSingleRelic(Relics original, Map<String, String> translationMap) {
        Relics translated = new Relics();
        // 复制基础属性
        translated.setUniqueName(original.getUniqueName());
        translated.setName(original.getName());
        translated.setCodexSecret(original.getCodexSecret());
        translated.setDescription(original.getDescription());

        // 翻译奖励列表
        List<RelicRewards> translatedRewards = original.getRelicRewards().stream()
                .map(reward -> translateReward(reward, translationMap))
                .collect(Collectors.toList());
        translated.setRelicRewards(translatedRewards);

        return translated;
    }

    /**
     * 翻译单个奖励
     */
    private RelicRewards translateReward(RelicRewards original, Map<String, String> translationMap) {
        RelicRewards translated = new RelicRewards();
        translated.setRarity(original.getRarity());
        translated.setTier(original.getTier());
        translated.setItemCount(original.getItemCount());

        // 获取翻译后名称
        String keyword = StringUtils.getLastThreeSegments(original.getRewardName());
        String translatedName = translationMap.get(keyword);

        // 如果没有找到翻译，记录未翻译项
        if (translatedName == null) {
            addToUntranslatedItems(original.getRewardName());
            translatedName = original.getRewardName(); // 使用原始名称
        }

        translated.setRewardName(translatedName);

        return translated;
    }

    /**
     * 添加未翻译项到集合（去重）
     */
    private void addToUntranslatedItems(String rewardName) {
        // 检查是否已存在相同的uniqueName，避免重复添加
        boolean exists = untranslatedItems.stream()
                .anyMatch(item -> item.get("uniqueName").equals(rewardName));

        if (!exists) {
            Map<String, String> item = new HashMap<>(3);
            item.put("uniqueName", rewardName);
            String name = StringUtils.getLastValueAfterSlash(rewardName);
            name = StringUtils.splitCamelCase(name)
                    .replace("Blueprint", "蓝图")
                    .replace("Systems", "系统")
                    .replace("Chassis", "机体")
                    .replace("Helmet", "头部神经光元")
            ;
            item.put("name", name); // 留空待翻译
            item.put("description", ""); // 留空待翻译
            untranslatedItems.add(item);
        }
    }

    /**
     * 导出未翻译项到JSON文件
     */
    private void exportUntranslatedItems() throws IOException {
        if (untranslatedItems.isEmpty()) {
            log.info("没有未翻译的奖励数据需要导出");
            return;
        }

        // 创建父目录（如果不存在）
        File file = new File("./data/UntranslatedRelicsRewardsName.json");
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 写入JSON数据
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            String json = JSON.toJSONString(untranslatedItems);
            writer.write(json);
            log.info("成功导出{}条未翻译数据到: {}", untranslatedItems.size(), "./UntranslatedItems.json");
        } catch (IOException e) {
            log.error("导出未翻译数据失败", e);
            throw e;
        }
    }

    /**
     * 分批次插入数据
     */
    private void batchInsertRelics(List<Relics> relics) {
        if (relics.isEmpty()) {
            log.info("没有需要插入的数据");
            return;
        }

        List<List<Relics>> batches = ListUtils.splitIntoBatches(relics, BATCH_SIZE);
        log.info("开始分批次插入，共{}批数据", batches.size());

        for (int i = 0; i < batches.size(); i++) {
            List<Relics> batch = batches.get(i);
            relicsRepository.saveAll(batch);
            log.info("完成第{}批插入，处理{}条记录", i + 1, batch.size());
        }
    }

}
