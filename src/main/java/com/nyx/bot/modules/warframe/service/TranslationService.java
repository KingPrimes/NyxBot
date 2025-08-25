package com.nyx.bot.modules.warframe.service;

import com.nyx.bot.modules.warframe.entity.NotTranslation;
import com.nyx.bot.modules.warframe.entity.Translation;
import com.nyx.bot.modules.warframe.repo.NotTranslationRepository;
import com.nyx.bot.modules.warframe.repo.TranslationRepository;
import com.nyx.bot.utils.MatcherUtils;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TranslationService {

    @Getter
    @Resource
    TranslationRepository repository;

    @Resource
    NotTranslationRepository ntr;


    /**
     * 新增 | 修改
     *
     * @param tra 不带ID新增，带ID修改
     */
    public Translation save(Translation tra) {
        return repository.saveAndFlush(tra);
    }

    /**
     * 精准匹配 英文到中文的翻译
     *
     * @param en 英文
     * @return 中文
     */
    public String enToZh(String en) {
        AtomicReference<String> cn = new AtomicReference<>(en.trim());
        repository.findByEn(cn.get()).ifPresent(t -> {
            if (!t.getCn().isEmpty()) {
                cn.set(t.getCn());
            }
        });
        if (cn.get().equals(en.trim())) {
            if (!MatcherUtils.isChines(en.trim())) {
                NotTranslation byNotTranslation = ntr.findByNotTranslation(en.trim());
                if (byNotTranslation == null) {
                    ntr.save(new NotTranslation(en.trim()));
                }
            }
        }
        return cn.get();
    }

    /**
     * 模糊匹配 英文到中文的翻译
     *
     * @param en 英文
     * @return 中文
     */
    public String enLikeZh(String en) {
        try {
            String cn = repository.findByEnLike(en.trim()).get(0).getCn();
            if (cn != null && !cn.isEmpty()) {
                return cn;
            }
            return en;
        } catch (Exception e) {
            return en;
        }
    }

    public String zhToEn(String zh_cn) {
        AtomicReference<String> cn = new AtomicReference<>(zh_cn.trim());
        repository.findByCn(zh_cn.trim()).ifPresent(t -> {
            if (!t.getEn().isEmpty()) {
                cn.set(t.getEn());
            }
        });
        return cn.get();
    }


}
