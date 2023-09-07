package com.nyx.bot.repo.impl.black;

import com.nyx.bot.entity.bot.black.GroupBlack;
import com.nyx.bot.entity.bot.black.PriveBlack;
import com.nyx.bot.repo.black.GroupBlackRepository;
import com.nyx.bot.repo.black.PriveBlackRepository;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class BlackService {

    @Autowired
    GroupBlackRepository repository;

    @Autowired
    PriveBlackRepository prepository;


    public GroupBlack findByGroupId(Long id) {
        AtomicReference<GroupBlack> gb = new AtomicReference<>(new GroupBlack());
        repository.findById(id).ifPresent(gb::set);
        return gb.get();
    }

    public PriveBlack findByPriveId(Long id) {
        AtomicReference<PriveBlack> pb = new AtomicReference<>();
        prepository.findById(id).ifPresent(pb::set);
        return pb.get();
    }


    public Page<GroupBlack> list(GroupBlack gb) {
        Specification<GroupBlack> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(gb.getGroupUid()).ifPresent(g -> {
                //模糊查询
                predicateList.add(cb.equal(root.get("groupUid"), g));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specs,
                PageRequest.of(gb.getPageNum() - 1, gb.getPageSize()));
    }

    public int save(GroupBlack gb) {
        AtomicInteger x = new AtomicInteger();
        Optional.ofNullable(gb).ifPresent(g -> {
            repository.save(g);
            x.set(1);
        });
        return x.get();
    }

    public int remove(GroupBlack gb) {
        AtomicInteger x = new AtomicInteger();
        Optional.ofNullable(gb.getId()).ifPresent(id -> {
            repository.deleteById(id);
            x.set(1);
        });
        return x.get();
    }


    public Page<PriveBlack> list(PriveBlack pb) {
        Specification<PriveBlack> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(pb.getUserUid()).ifPresent(p -> {
                //模糊查询
                predicateList.add(cb.equal(root.get("userUid"), p));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return prepository.findAll(specs,
                PageRequest.of(pb.getPageNum() - 1, pb.getPageSize()));
    }

    public int save(PriveBlack pb) {
        AtomicInteger x = new AtomicInteger();
        Optional.ofNullable(pb).ifPresent(p -> {
            prepository.save(pb);
            x.set(1);
        });
        return x.get();
    }

    public int remove(PriveBlack pb) {
        AtomicInteger x = new AtomicInteger();
        Optional.ofNullable(pb.getId()).ifPresent(id -> {
            prepository.deleteById(id);
            x.set(1);
        });
        return x.get();
    }

    /**
     * 判断是否是黑名单
     *
     * @param groupUid 群号
     * @param userUid  qq号
     * @return false黑名单
     */
    public boolean isBlack(Long groupUid, Long userUid) {
        AtomicBoolean flag = new AtomicBoolean(true);
        if (groupUid != 0L) {
            GroupBlack byGroupUid = repository.findByGroupUid(groupUid);
            Optional.ofNullable(byGroupUid).ifPresent(g -> {
                flag.set(false);
            });
        }
        PriveBlack byUserUid = prepository.findByUserUid(userUid);
        Optional.ofNullable(byUserUid).ifPresent(p -> {
            flag.set(false);
        });
        return flag.get();
    }

}
