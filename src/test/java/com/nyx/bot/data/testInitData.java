package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.NyxBotApplicationTest;
import com.nyx.bot.entity.warframe.exprot.Weapons;
import com.nyx.bot.repo.warframe.exprot.WeaponsRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class testInitData {

    @Resource
    WeaponsRepository repository;

    @Test
    void initAlias() {
        WarframeDataSource.getRivenTrend();
    }

    @Test
    void initTranslation() {
        WarframeDataSource.initTranslation();
    }

    @Test
    void test() {
//        JSONObject jsonObject = JSON.parseObject(new FileInputStream("D:\\Demos\\NyxBot\\data\\phpData\\ExportWeapons_zh.json").readAllBytes());
//        List<Weapons> exportWeapons = jsonObject.getList("ExportWeapons", Weapons.class);
//        List<Weapons> exportRailjackWeapons = jsonObject.getList("ExportRailjackWeapons", Weapons.class);
//        repository.saveAll(exportWeapons);
//        repository.saveAll(exportRailjackWeapons);
        Weapons weapons = repository.findById("/Lotus/Weapons/Infested/InfestedLich/Pistols/CodaCatabolyst").orElse(new Weapons());
        log.info("{}", weapons.getDamagePerShotList());
        log.info("{}", JSON.toJSONString(weapons));
    }

}
