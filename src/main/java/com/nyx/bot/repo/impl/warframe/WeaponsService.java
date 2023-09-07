package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.entity.warframe.Weapons;
import com.nyx.bot.repo.warframe.WeaponsRepository;
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

@Slf4j
@Service
public class WeaponsService {

    @Autowired
    WeaponsRepository repository;


    /**
     * 分页查询
     */
    public Page<Weapons> list(Weapons w) {
        Specification<Weapons> specification = (root, query, bu) -> {

            List<Predicate> predicateList = new ArrayList<>();

            Optional.ofNullable(w.getItemName()).ifPresent(
                    itemName -> {
                        if (!itemName.isEmpty()) {
                            predicateList.add(bu.like(root.get("itemName"), "%" + itemName + "%"));
                        }
                    }
            );
            Optional.ofNullable(w.getUrlName()).ifPresent(
                    urlName -> {
                        if (!urlName.isEmpty()) {
                            predicateList.add(bu.like(root.get("urlName"), "%" + urlName + "%"));
                        }
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specification, PageRequest.of(w.getPageNum() - 1, w.getPageSize()));
    }
}
