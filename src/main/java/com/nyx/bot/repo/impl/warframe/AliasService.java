package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.repo.warframe.AliasRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AliasService {
    @Autowired
    private AliasRepository repository;

    /**
     * 分页查询
     *
     * @param alias 条件
     * @return 分页数据
     */
    public Page<Alias> list(Alias alias) {
        Specification<Alias> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(alias.getEn()).ifPresent(e -> {
                if (!e.trim().isEmpty()) {
                    //模糊查询
                    predicateList.add(cb.like(root.get("en").as(String.class), "%" + e + "%"));
                }
            });
            Optional.ofNullable(alias.getCn()).ifPresent(c -> {
                //模糊查询
                predicateList.add(cb.like(root.get("cn").as(String.class), "%" + c + "%"));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specs,
                PageRequest.of(alias.getPageNum() - 1, alias.getPageSize()));
    }

    /**
     * 根据ID查询
     *
     * @param id id
     */
    public Alias findById(Long id) {
        AtomicReference<Alias> alias = new AtomicReference<>();
        repository.findById(id).ifPresent(alias::set);
        return alias.get();
    }


}
