package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.utils.ApiDataSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class StateTranslationService {

    private final ApiDataSourceUtils apiDataSourceUtils;

    private final StateTranslationRepository str;

    public StateTranslationService(ApiDataSourceUtils apiDataSourceUtils, StateTranslationRepository str) {
        this.apiDataSourceUtils = apiDataSourceUtils;
        this.str = str;
    }

    public List<StateTranslation> getStateTranslationsForCnd() {
        return apiDataSourceUtils.getDataFromSources(ApiUrl.WARFRAME_DATA_SOURCE_STATE_TRANSLATION, new TypeReference<>() {
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

}
