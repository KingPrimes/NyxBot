package com.nyx.bot.repo.impl.white;

import com.nyx.bot.entity.bot.white.GroupWhite;
import com.nyx.bot.entity.bot.white.ProveWhite;
import com.nyx.bot.repo.bot.white.GroupWhiteRepository;
import com.nyx.bot.repo.bot.white.ProveWhiteRepository;
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

@Slf4j
@Service
public class WhiteService {

    @Resource
    GroupWhiteRepository groupWhiteRepository;

    @Resource
    ProveWhiteRepository proveWhiteRepository;


    public GroupWhite findByGroup(Long group) {
        return groupWhiteRepository.findByGroupUid(group);
    }

    public ProveWhite findByProve(Long prove) {
        return proveWhiteRepository.findByProve(prove);
    }

    public Page<GroupWhite> list(GroupWhite gb) {
        Specification<GroupWhite> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(gb.getGroupUid()).ifPresent(g -> {
                //模糊查询
                predicateList.add(cb.equal(root.get("group"), g));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return groupWhiteRepository.findAll(specs,
                PageRequest.of(gb.getPageNum() - 1, gb.getPageSize()));
    }

    public Page<ProveWhite> list(ProveWhite gb) {
        Specification<ProveWhite> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(gb.getProve()).ifPresent(g -> {
                //模糊查询
                predicateList.add(cb.equal(root.get("prove"), g));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return proveWhiteRepository.findAll(specs,
                PageRequest.of(gb.getPageNum() - 1, gb.getPageSize()));
    }

    public GroupWhite save(GroupWhite groupWhite) {
        return groupWhiteRepository.save(groupWhite);
    }

    public ProveWhite save(ProveWhite proveWhite) {
        return proveWhiteRepository.save(proveWhite);
    }

    public void remove(GroupWhite groupWhite) {
        groupWhiteRepository.deleteById(groupWhite.getId());
    }

    public void remove(ProveWhite proveWhite) {
        proveWhiteRepository.deleteById(proveWhite.getId());
    }

    public boolean isWhite(Long group, Long prove) {
        boolean flag = false;
        if (group != null && group != 0L) {
            GroupWhite byGroup = groupWhiteRepository.findByGroupUid(group);
            if (byGroup != null) {
                flag = true;
            }
        }
        ProveWhite byProve = proveWhiteRepository.findByProve(prove);
        if (byProve != null) {
            flag = true;
        }
        return flag;
    }

}
