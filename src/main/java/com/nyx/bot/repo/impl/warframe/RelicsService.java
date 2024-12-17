package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.Relics;
import com.nyx.bot.repo.warframe.AliasRepository;
import com.nyx.bot.repo.warframe.RelicsRepository;
import com.nyx.bot.repo.warframe.RelicsRewardsRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class RelicsService {

    @Resource
    RelicsRepository repository;

    @Resource
    RelicsRewardsRepository rwrepository;

    @Resource
    AliasRepository ar;

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
        return new TableDataInfo(
                200,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                translate(page.getContent())
        );
    }


    public List<Relics> findAllByRelicNameOrRewardsItemName(String name) {
        // 默认查询遗物名称
        var rs = repository.findByRelicName(name);
        if (!rs.isEmpty()) return translate(rs);
        // 如果遗物名称未查询到，进行奖励物品的查询
        var rws = rwrepository.findByItemName(name);
        if (!rws.isEmpty()) {
            List<Relics> rList = new ArrayList<>();
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
            return translate(rList);
        }
        // 如果精准查询为查询到，进行模糊查询
        rws = rwrepository.findByItemNameLike(name);
        if (!rws.isEmpty()) {
            List<Relics> rList = new ArrayList<>();
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
            return translate(rList);
        }
        if (name.toLowerCase().contains("prime")) {
            name = name.replace("prime", " Prime ");
        }
        if (name.toLowerCase().contains("p")) {
            name = name.replace("p", " Prime ");
        }
        // 如果奖励物品名称未查询到，进行别名查询
        var alias = ar.findAll();
        AtomicReference<String> key = new AtomicReference<>(name);
        alias.forEach(a -> {
            if (key.get().contains(a.getCn())) {
                key.set(key.get().replace(a.getCn(), a.getEn()));
            }
        });
        log.info("result:{}", key.get());
        rws = rwrepository.findByItemNameLike(key.get());
        if (!rws.isEmpty()) {
            List<Relics> rList = new ArrayList<>();
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
            return translate(rList);
        }
        rws = rwrepository.findByItemNameLike(tr.zhToEn(name));
        if (!rws.isEmpty()) {
            List<Relics> rList = new ArrayList<>();
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
            return translate(rList);
        }

        rws = rwrepository.findByItemNameLike(tr.zhToEn(name));
        if (!rws.isEmpty()) {
            List<Relics> rList = new ArrayList<>();
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
            return translate(rList);
        }
        return new ArrayList<>();
    }

    /**
     * 翻译
     *
     * @param rs 数据集合
     * @return 处理后的数据
     */
    private List<Relics> translate(List<Relics> rs) {
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
        // 合并结果,并返回
        return s1.thenCombine(s2, (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toList())).join();
    }
}
