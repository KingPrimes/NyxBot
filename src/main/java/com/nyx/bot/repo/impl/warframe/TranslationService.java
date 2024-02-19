package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.NotTranslation;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.warframe.NotTranslationRepository;
import com.nyx.bot.repo.warframe.TranslationRepository;
import com.nyx.bot.utils.MatcherUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TranslationService {

    @Resource
    TranslationRepository repository;

    @Resource
    NotTranslationRepository ntr;

    /**
     * 分页查询
     */
    public Page<Translation> list(Translation tra) {
        Specification<Translation> specification = (root, query, bu) -> {

            List<Predicate> predicateList = new ArrayList<>();
            Optional.ofNullable(tra.getCn()).ifPresent(cn -> {
                        if (!cn.isEmpty()) {
                            predicateList.add(bu.like(root.get("cn"), "%" + cn + "%"));
                        }
                    }
            );
            Optional.ofNullable(tra.getEn()).ifPresent(en -> {
                        if (!en.isEmpty()) {
                            predicateList.add(bu.like(root.get("en"), "%" + en + "%"));
                        }
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specification, PageRequest.of(tra.getPageNum() - 1, tra.getPageSize()));
    }

    /**
     * 根据ID查询
     */
    public Translation findById(Long id) {
        AtomicReference<Translation> translation = new AtomicReference<>();
        repository.findById(id).ifPresent(translation::set);
        return translation.get();
    }

    /**
     * 新增 | 修改
     *
     * @param tra 不带ID新增，带ID修改
     */
    public Translation save(Translation tra) {
        return repository.save(tra);
    }

    /**
     * 查询最大ID
     */
    public Translation maxId() {
        //查询最大ID
        return repository.findTopByOrderByIdDesc();

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
                ntr.save(new NotTranslation(en));
            }
            return en;
        } catch (Exception e) {
            try {
                if (!MatcherUtils.isChines(en)) {
                    ntr.save(new NotTranslation(en));
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

    /**
     * 模糊匹配 英文到中文的翻译 获取模糊匹配的翻译列表
     *
     * @param en 英文
     * @return 中文
     */
    public List<Translation> enLikeZhList(String en) {
        return repository.findByEnLike(en);
    }


    public List<Translation> findAllToList() {
        return repository.findAll();
    }

}
