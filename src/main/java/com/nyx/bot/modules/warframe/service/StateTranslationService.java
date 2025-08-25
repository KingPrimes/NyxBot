package com.nyx.bot.modules.warframe.service;

import com.nyx.bot.modules.warframe.entity.NotTranslation;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.repo.NotTranslationRepository;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;
@SuppressWarnings("unused")
@Slf4j
@Service
public class StateTranslationService {
    @Resource
    StateTranslationRepository str;
    @Resource
    NotTranslationRepository ntr;


    /**
     * 新增 | 修改
     *
     * @param st 不带ID新增，带ID修改
     */
    public StateTranslation save(StateTranslation st) {
        return str.save(st);
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
        if (rest.get().equals(uniqueName)) {
            NotTranslation byNotTranslation = ntr.findByNotTranslation(uniqueName);
            if (byNotTranslation == null) {
                log.debug("未找到翻译 -- {}", uniqueName);
                ntr.save(new NotTranslation(uniqueName));
            }
        }
        return rest.get();
    }

}
