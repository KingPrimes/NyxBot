package com.nyx.bot.repo.impl;

import com.nyx.bot.entity.bot.IssueReply;
import com.nyx.bot.repo.IssueReplyRepository;
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
public class IssueReplyService {

    @Autowired
    IssueReplyRepository repository;


    /**
     * 分页查询
     *
     * @param ir 条件
     * @return 分页数据
     */
    public Page<IssueReply> list(IssueReply ir) {
        Specification<IssueReply> specs = (root, query, cb) -> {
            // 创建查询列表
            List<Predicate> predicateList = new ArrayList<>();
            //判断是否是Null
            Optional.ofNullable(ir.getIssue()).ifPresent(i -> {
                if (!i.trim().isEmpty()) {
                    //模糊查询
                    predicateList.add(cb.like(root.get("issue").as(String.class), "%" + i + "%"));
                }
            });
            Optional.ofNullable(ir.getReply()).ifPresent(r -> {
                //模糊查询
                predicateList.add(cb.like(root.get("reply").as(String.class), "%" + r + "%"));
            });
            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specs,
                PageRequest.of(ir.getPageNum() - 1, ir.getPageSize()));
    }
}
