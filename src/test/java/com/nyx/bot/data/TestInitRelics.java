package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.Relics;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.impl.warframe.RelicsService;
import com.nyx.bot.repo.warframe.RelicsRepository;
import com.nyx.bot.repo.warframe.RelicsRewardsRepository;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestInitRelics {

    @Resource
    RelicsRepository repository;

    @Resource
    RelicsRewardsRepository rewardsRepository;

    @Resource
    RelicsService rservice;

    @Test
    void testInitRelics() {
        HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_RELICS_DATA);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            List<Relics> relics = JSON.parseObject(body.getBody()).getJSONArray("relics").toJavaList(Relics.class).stream().filter(r -> r.getState().equals("Intact")).toList();
            relics = relics.stream().peek(r -> r.setRewards(r.getRewards().stream().peek(w -> w.setRelics(r)).toList())).toList();
            repository.saveAll(relics);
        }

    }

    @Test
    void testSelectRelicsById() {
        repository.findById("c3e750e6d2f820f53ff15c98a151413a").ifPresent(r -> log.info(r.toString()));
    }

    @Test
    void testSelectRelicsByName() {
        repository.findByRelicName("A11").forEach(System.out::println);
    }

    @Test
    void testSelectRelicsByTier() {
        //repository.findByTier("Axi").forEach(System.out::println);
        // 随机获取 4 条数据并且Tier等于Axi
        repository.findByTier("Axi").stream().skip((int) (Math.random() * 4)).limit(4).forEach(System.out::println);

    }

    @Test
    void testSelectRelicsByRewards() {
        rewardsRepository.findById(8L).ifPresent(r -> log.info(r.toString()));
    }

    @Test
    void testSelectRelicsByRewardsName() {
        rewardsRepository.findByItemName("Zakti Prime Receiver").forEach(System.out::println);
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
    void testFindAllByRelicNameAndTier() {
        repository.findByRelicNameAndTier("A11", "Axi").forEach(System.out::println);
    }

    @Test
    void testFindAllByRelicNameOrRewardsItemName() {
        var rs = repository.findByRelicName("");
        List<Relics> rList = new ArrayList<>();
        if (!rs.isEmpty()) {
            rs.forEach(System.out::println);
        } else {
            var rws = rewardsRepository.findByItemName("Zakti Prime Receiver");
            rws.forEach(w -> {
                repository.findById(w.getRelics().getRelicsId()).ifPresent(rList::add);
            });
        }
        rList.forEach(System.out::println);
    }

    @Test
    void testFindByItemNameLike() {
        System.out.println(JSON.toJSONString(rservice.findAllByRelicNameOrRewardsItemName("牛p头")));
    }
}
