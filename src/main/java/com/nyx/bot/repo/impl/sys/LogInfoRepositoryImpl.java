package com.nyx.bot.repo.impl.sys;

import com.nyx.bot.entity.sys.LogInfo;
import com.nyx.bot.repo.sys.LogInfoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LogInfoRepositoryImpl {
    @Autowired
    LogInfoRepository repository;

    public Page<LogInfo> list(LogInfo info) {
        Specification<LogInfo> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(info.getCodes()).ifPresent(codes -> {
                if (!codes.trim().isEmpty()) {
                    //添加相等查询
                    predicateList.add(cb.equal(root.get("codes"), info.getCodes()));
                }
            });
            Optional.ofNullable(info.getGroupUid()).ifPresent(group -> {
                //模糊查询
                predicateList.add(cb.like(root.get("groupUid").as(String.class), "%" + info.getGroupUid() + "%"));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specs,
                PageRequest.of(info.getPageNum() - 1, info.getPageSize()));
    }

    public AtomicReference<LogInfo> findById(Long logId) {
        AtomicReference<LogInfo> logInfo = new AtomicReference<>();
        repository.findById(logId).ifPresent(logInfo::set);
        return logInfo;
    }


}
