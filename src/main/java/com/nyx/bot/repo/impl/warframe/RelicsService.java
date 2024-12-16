package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.Relics;
import com.nyx.bot.repo.warframe.RelicsRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class RelicsService {

    @Resource
    RelicsRepository repository;

    @Resource
    TranslationService tr;


    public TableDataInfo findAllPageable(Relics relics) {
        if (relics.getRelicName().isEmpty()) relics.setRelicName(null);
        var page = repository.findAllPageable(relics,
                PageRequest.of(
                        relics.getPageNum() - 1,
                        relics.getPageSize()
                )
        );

        List<Relics> rs = page.getContent();

        // 将列表分成两部分，进行多线程处理
        var ref = new Object() {
            final List<Relics> s1 = rs.subList(0, rs.size() / 2);
            final List<Relics> s2 = rs.subList(rs.size() / 2, rs.size());
        };
        // 处理1
        var s1 = CompletableFuture.supplyAsync(() -> ref.s1.stream().peek(r -> {
            r.setRewards(r.getRewards().stream().peek(w -> {
                w.setItemName(tr.enToZh(w.getItemName()));
                w.setRarity(tr.enToZh(w.getRarity()));
            }).toList());
            r.setTier(tr.enToZh(r.getTier()));
        }).toList());
        // 处理2
        var s2 = CompletableFuture.supplyAsync(() -> ref.s2.stream().peek(r -> {
            r.setRewards(r.getRewards().stream().peek(w -> {
                w.setItemName(tr.enToZh(w.getItemName()));
                w.setRarity(tr.enToZh(w.getRarity()));
            }).toList());
            r.setTier(tr.enToZh(r.getTier()));
        }).toList());
        // 合并结果
        CompletableFuture<List<Relics>> combinedFuture = s1.thenCombine(s2, (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList()));

        return new TableDataInfo(
                200,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                combinedFuture.join()
        );
    }

}
