package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
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
public class OrdersItemsService {

    @Autowired
    OrdersItemsRepository repository;


    /**
     * 获取最大ID
     */
    public Long maxId() {
        return repository.findTopByOrderByOidDesc().getOid();
    }


    /**
     * 新增 | 修改
     */
    public OrdersItems save(OrdersItems items) {
        return repository.save(items);
    }

    /**
     * 根据ID查询
     */
    public OrdersItems findById(Long id) {
        AtomicReference<OrdersItems> items = new AtomicReference<>();
        repository.findById(id).ifPresent(items::set);
        return items.get();
    }

    public List<OrdersItems> findAll() {
        return repository.findAll();
    }


    /**
     * 分页查询
     */
    public Page<OrdersItems> list(OrdersItems items) {
        Specification<OrdersItems> specification = (root, query, bu) -> {

            List<Predicate> predicateList = new ArrayList<>();

            Optional.ofNullable(items.getItemName()).ifPresent(
                    name -> {
                        predicateList.add(bu.like(root.get("itemName"), "%" + name + "%"));
                    }
            );

            Predicate[] predicates = new Predicate[predicateList.size()];
            return query.where(predicateList.toArray(predicates)).getRestriction();
        };
        return repository.findAll(specification, PageRequest.of(items.getPageNum() - 1, items.getPageSize()));
    }

}
