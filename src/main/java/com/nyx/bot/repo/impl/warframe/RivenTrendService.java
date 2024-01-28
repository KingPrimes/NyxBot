package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.repo.warframe.RivenTrendRepository;
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
public class RivenTrendService {

    @Autowired
    RivenTrendRepository repository;

    /**
     * 分页查询
     */
    public Page<RivenTrend> list(RivenTrend items) {
        Specification<RivenTrend> specification = (root, query, bu) -> {

            List<Predicate> predicateList = new ArrayList<>();

            Optional.ofNullable(items.getTrendName()).ifPresent(
                    trendName -> {
                        if (!trendName.isEmpty()) {
                            predicateList.add(bu.like(root.get("trendName"), "%" + trendName + "%"));
                        }
                    }
            );
            Optional.ofNullable(items.getNewDot()).ifPresent(
                    newDot -> {
                        if (!newDot.isEmpty()) {
                            predicateList.add(bu.equal(root.get("newDot"), newDot));
                        }
                    }
            );
            Optional.ofNullable(items.getOldDot()).ifPresent(
                    oldDot -> {
                        if (!oldDot.isEmpty()) {
                            predicateList.add(bu.like(root.get("oldDot"), oldDot));
                        }
                    }
            );
            Optional.ofNullable(items.getType()).ifPresent(
                    type -> {
                        predicateList.add(bu.like(root.get("type"), "%" + type + "%"));
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specification, PageRequest.of(items.getPageNum() - 1, items.getPageSize()));
    }

}
