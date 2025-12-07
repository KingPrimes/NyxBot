package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class StateTranslationService {

    ObjectMapper objectMapper;

    StateTranslationRepository str;

    public StateTranslationService(ObjectMapper objectMapper, StateTranslationRepository str) {
        this.objectMapper = objectMapper;
        this.str = str;
    }

    public List<StateTranslation> getStateTranslationsForCnd() {
        List<StateTranslation> list = new ArrayList<>();
        for (String url : ApiUrl.WARFRAME_DATA_SOURCE_STATE_TRANSLATION) {
            HttpUtils.Body body = HttpUtils.sendGet(url);
            if (body.code().is2xxSuccessful()) {
                try {
                    list.addAll(objectMapper.readValue(
                            body.body(),
                            new TypeReference<>() {
                            }
                    ));
                    break;
                } catch (Exception e) {
                    log.warn("解析 StateTranslation 数据失败，尝试下一个数据源: {}", e.getMessage());
                }
            } else {
                log.warn("获取 StateTranslation 数据失败,，尝试下一个数据源: HttpCode {} - Url:{}", body.code(), url);
            }
        }
        return list;
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
