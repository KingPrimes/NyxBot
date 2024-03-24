package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.NotTranslation;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.warframe.NotTranslationRepository;
import com.nyx.bot.repo.warframe.TranslationRepository;
import com.nyx.bot.utils.MatcherUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TranslationService {

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

    public TranslationRepository getRepository() {
        return repository;
    }

    /**
     * 精准匹配 英文到中文的翻译
     *
     * @param en 英文
     * @return 中文
     */
    public String enToZh(String en) {
        try {
            String cn = repository.findByEn(en).getCn();
            if (cn != null && !cn.isEmpty()) {
                return cn;
            }
            if (!MatcherUtils.isChines(en)) {
                NotTranslation byNotTranslation = ntr.findByNotTranslation(en);
                if (byNotTranslation == null) {
                    ntr.save(new NotTranslation(en));
                }
            }
            return en;
        } catch (Exception e) {
            try {
                if (!MatcherUtils.isChines(en)) {
                    NotTranslation byNotTranslation = ntr.findByNotTranslation(en);
                    if (byNotTranslation == null) {
                        ntr.save(new NotTranslation(en));
                    }
                }
            } catch (Exception ignored) {
                return en;
            }
            return en;
        }
    }

    /**
     * 模糊匹配 英文到中文的翻译
     *
     * @param en 英文
     * @return 中文
     */
    public String enLikeZh(String en) {
        try {
            String cn = repository.findByEnLike(en).get(0).getCn();
            if (cn != null && !cn.isEmpty()) {
                return cn;
            }
            return en;
        } catch (Exception e) {
            return en;
        }
    }

    public String zhToEn(String zh_cn) {
        try {
            Translation byCn = repository.findByCn(zh_cn.trim());
            if (!byCn.getEn().isEmpty()) {
                return byCn.getEn();
            } else {
                return zh_cn;
            }
        } catch (Exception ignored) {
            return zh_cn;
        }
    }


}
