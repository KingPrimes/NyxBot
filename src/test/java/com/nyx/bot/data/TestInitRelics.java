package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.entity.warframe.exprot.Relics;
import com.nyx.bot.repo.impl.warframe.RelicsService;
import com.nyx.bot.repo.warframe.exprot.RelicsRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestInitRelics {

    @Resource
    RelicsRepository repository;

    @Resource
    RelicsService rservice;

    @Test
    void testSelectRelicsById() {
        repository.findById("c3e750e6d2f820f53ff15c98a151413a").ifPresent(r -> log.info(r.toString()));
    }

    @Test
    void testSelectRelicsByName() {
        repository.findByNameContaining("A11").forEach(System.out::println);
    }

    @Test
    void testSelectRelicsByRewardsName() {
        repository.findByRelicRewardsRewardNameContaining("Zakti Prime Receiver").forEach(System.out::println);
    }

    @Test
    void testFindAllPageable() {
        Page<Relics> page = repository.findAllPageable(new Relics(), PageRequest.of(
                10,
                20
        ));
        log.info("getTotalPages:{}", page.getTotalPages());
        log.info("getTotalElements:{}", page.getTotalElements());
        log.info("getContent:{}", page.getContent());
    }
    @Test
    void testFindByItemNameLike() {
        System.out.println(JSON.toJSONString(rservice.findAllByRelicNameOrRewardsItemName("牛p头")));
    }
}
