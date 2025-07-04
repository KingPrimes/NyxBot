package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.entity.warframe.StateTranslation;
import com.nyx.bot.entity.warframe.exprot.Weapons;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.repo.warframe.StateTranslationRepository;
import com.nyx.bot.repo.warframe.exprot.WeaponsRepository;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@SpringBootTest(classes = NyxBotApplicationTest.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestInitData {

    @Resource
    WeaponsRepository repository;

    @Resource
    StateTranslationRepository str;

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
        Weapons weapons = repository.findById("/Lotus/Weapons/Infested/InfestedLich/Pistols/CodaCatabolyst").orElse(new Weapons());
        log.info("{}", weapons.getDamagePerShotList());
        log.info("{}", JSON.toJSONString(weapons));
    }

    @Test
    void testInitExprot() throws IOException {
        String path = "./data/lzma/index_zh.txt.lzma";
        String outPath = "./data/lzma/index_zh.txt";
//        Boolean zh = HttpUtils.sendGetForFile(ApiUrl.WARFRAME_PUBLIC_EXPORT_INDEX.formatted("zh"), path);
//        if (!zh) {
//            return;
//        }
//        ZipUtils.unLzma(path, outPath);
        List<String> keys = FileUtils.readFileToList(outPath);
        List<StateTranslation> st = new ArrayList<>();
        for (String key : keys) {
            log.info(key);

            // 外观
            if (key.contains("ExportCustoms_zh")) {
                HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_PUBLIC_EXPORT_MANIFESTS.formatted(key));
                log.info("BodyCode:ExportCustoms_zh --- {}", body.getCode());
                if (body.getCode() == HttpCodeEnum.SUCCESS) {
                    List<StateTranslation> customs = JSONObject.parseObject(body.getBody()).getList("ExportCustoms", StateTranslation.class)
                            .stream()
                            .peek(s -> {
                                Arrays.stream(StateTypeEnum.values())
                                        .filter(stateTypeEnum -> s.getUniqueName().contains(stateTypeEnum.getKEY()))
                                        .findFirst()
                                        .ifPresent(s::setType);
                                s.setTradable(false);
                            })
                            .toList();
                    log.info("customs:{}", JSON.toJSONString(customs));
                    st.addAll(customs);
                }
            }
            // 采集器
            if (key.contains("ExportDrones_zh")) {
                HttpUtils.Body body = HttpUtils.sendGet(ApiUrl.WARFRAME_PUBLIC_EXPORT_MANIFESTS.formatted(key));
                log.info("BodyCode:ExportDrones_zh --- {}", body.getCode());
                if (body.getCode() == HttpCodeEnum.SUCCESS) {
                    List<StateTranslation> drones = JSONObject.parseObject(body.getBody()).getList("ExportDrones", StateTranslation.class)
                            .stream()
                            .peek(s -> {
                                Arrays.stream(StateTypeEnum.values())
                                        .filter(stateTypeEnum -> s.getUniqueName().contains(stateTypeEnum.getKEY()))
                                        .findFirst()
                                        .ifPresent(s::setType);
                                s.setTradable(false);
                            })
                            .toList();
                    log.info("drones:{}", JSON.toJSONString(drones));
                    st.addAll(drones);
                }

            }

        }
        log.info("StateTranslationList:{}", JSON.toJSONString(st));

    }

}
