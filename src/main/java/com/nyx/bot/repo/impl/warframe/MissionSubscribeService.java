package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.repo.warframe.MissionSubscribeRepository;
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
public class MissionSubscribeService {

    @Autowired
    MissionSubscribeRepository mir;

    public Page<MissionSubscribe> list(MissionSubscribe ms) {
        Specification<MissionSubscribe> sp = (r, q, b) -> {
            List<Predicate> predicateList = new ArrayList<>();
            Optional.ofNullable(ms.getSubGroup()).ifPresent(subGroup -> {
                        if (!subGroup.equals(0L)) {
                            predicateList.add(b.like(r.get("subGroup"), "%" + subGroup + "%"));
                        }
                    }
            );
            Optional.ofNullable(ms.getSubUser()).ifPresent(subUser -> {
                        if (!subUser.isEmpty()) {
                            predicateList.add(b.like(r.get("subUser"), "%" + subUser + "%"));
                        }
                    }
            );
            Optional.ofNullable(ms.getSubBotUid()).ifPresent(subBotUid -> {
                        if (!subBotUid.equals(0L)) {
                            predicateList.add(b.like(r.get("subBotUid"), "%" + subBotUid + "%"));
                        }
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return q.where(predicateList.toArray(predicates)).getRestriction();
        };
        return mir.findAll(sp, PageRequest.of(ms.getPageNum() - 1, ms.getPageSize()));
    }

}
