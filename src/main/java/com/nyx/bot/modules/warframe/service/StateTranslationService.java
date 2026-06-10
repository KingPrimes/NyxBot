package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.data.ExportFilePath;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.utils.ApiDataSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class StateTranslationService {

    private static final List<ExportSource> EXPORT_SOURCES = List.of(
            new ExportSource("ExportCustoms", "ExportCustoms", StateTypeEnum.ALL),
            new ExportSource("ExportDrones", "ExportDrones", StateTypeEnum.ALL),
            new ExportSource("ExportFlavour", "ExportFlavour", StateTypeEnum.ALL),
            new ExportSource("ExportGear", "ExportGear", StateTypeEnum.GEAR),
            new ExportSource("ExportKeys", "ExportKeys", StateTypeEnum.KEYS),
            new ExportSource("ExportRelicArcane", "ExportRelicArcane", StateTypeEnum.ALL),
            new ExportSource("ExportResources", "ExportResources", StateTypeEnum.RESOURCES),
            new ExportSource("ExportSentinels", "ExportSentinels", StateTypeEnum.SENTINELS),
            new ExportSource("ExportSortieRewards", "ExportOther", StateTypeEnum.OTHER),
            new ExportSource("ExportUpgrades", "ExportUpgrades", StateTypeEnum.MODS),
            new ExportSource("ExportWarframes", "ExportWarframes", StateTypeEnum.WARFRAMES),
            new ExportSource("ExportWeapons", "ExportWeapons", StateTypeEnum.WEAPONS)
    );
    private final ApiDataSourceUtils apiDataSourceUtils;
    private final StateTranslationRepository str;
    private final ObjectMapper objectMapper;

    public StateTranslationService(ApiDataSourceUtils apiDataSourceUtils, StateTranslationRepository str,
                                   ObjectMapper objectMapper) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.str = str;
        this.objectMapper = objectMapper;
    }

    public List<StateTranslation> getStateTranslationsForCnd() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.warframeDataSourceStateTranslation(), new TypeReference<>() {
        });
    }

    /**
     * 新增 | 修改
     *
     * @param st 不带ID新增，带ID修改
     */
    @SuppressWarnings("null")
    public StateTranslation save(StateTranslation st) {
        return str.saveAndFlush(st);
    }

    /**
     * 获取名称
     *
     * @param uniqueName 唯一名称
     * @return 名称
     */
    public String getName(String uniqueName) {
        AtomicReference<String> rest = new AtomicReference<>(uniqueName);
        str.findByUniqueName(uniqueName).ifPresent(st -> {
            if (!st.getName().isEmpty()) {
                rest.set(st.getName());
            }
        });
        return rest.get();
    }

    /**
     * 从导出文件 + CDN 初始化翻译数据（路径内部解析）
     */
    @Transactional
    public void initData() {
        log.info("开始初始化 Lost 翻译 数据！");
        List<StateTranslation> all = new ArrayList<>();
        for (ExportSource source : EXPORT_SOURCES) {
            all.addAll(parseFromExport(
                    ExportFilePath.resolve(source.prefix), source.key, source.type));
        }
        int size = str.saveAll(all).size();
        log.info("初始化 Lost 翻译 数据完成，共{}条", size);
        initFromCdn();
        log.info("初始化 Lost 翻译 数据全部完成");
    }

    /**
     * 从 CDN 初始化自定义翻译数据（由 initData() 内部调用，事务与 initData() 共享）
     */
    private void initFromCdn() {
        log.info("开始初始化 自定义 Lost state_translation.json 翻译 数据！");
        List<StateTranslation> javaList = getStateTranslationsForCnd();
        List<StateTranslation> sts = javaList.stream().peek(s -> {
            Arrays.stream(StateTypeEnum.values())
                    .filter(stateTypeEnum -> s.getUniqueName()
                            .matches(stateTypeEnum.getKEY()))
                    .findFirst()
                    .ifPresentOrElse(s::setType, () -> s.setType(StateTypeEnum.RESOURCES));
        }).toList();
        int size = str.saveAll(sts).size();
        log.info("初始化 自定义 Lost state_translation.json 翻译 数据完成，共{}条", size);
    }

    private List<StateTranslation> parseFromExport(String exportPath, String key, StateTypeEnum typeEnum) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(new FileInputStream(exportPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            JsonNode arrayNode = rootNode.get(key);
            List<StateTranslation> list = objectMapper.convertValue(
                    arrayNode,
                    new TypeReference<>() {
                    });
            return list.stream().filter(s -> !s.getName().isEmpty()).peek(s -> {
                s.setType(StateTypeEnum.RESOURCES);
                Arrays.stream(StateTypeEnum.values())
                        .filter(stateTypeEnum -> s.getUniqueName().matches(stateTypeEnum.getKEY()))
                        .findFirst()
                        .ifPresentOrElse(s::setType, () -> s.setType(typeEnum));
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + key + " from " + exportPath, e);
        }
    }

    /**
     * 导出文件源信息（prefix → ExportFilePath.resolve 解析）
     */
    private record ExportSource(String prefix, String key, StateTypeEnum type) {
    }

}
