package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.Ephemeras;
import com.nyx.bot.repo.warframe.EphemerasRepository;
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
public class EphemerasService {

    @Autowired
    EphemerasRepository repository;

    public Page<Ephemeras> list(Ephemeras el) {
        Specification<Ephemeras> specification = (root, query, bu) -> {
            List<Predicate> predicateList = new ArrayList<>();
            Optional.ofNullable(el.getItemName()).ifPresent(name -> {
                        if (!name.isEmpty()) {
                            predicateList.add(bu.like(root.get("itemName"), "%" + name + "%"));
                        }
                    }
            );
            Optional.ofNullable(el.getUrlName()).ifPresent(urlName -> {
                        if (!urlName.isEmpty()) {
                            predicateList.add(bu.like(root.get("urlName"), "%" + urlName + "%"));
                        }
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specification, PageRequest.of(el.getPageNum() - 1, el.getPageSize()));
    }

}
