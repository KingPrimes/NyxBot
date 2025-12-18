package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.repo.LichSisterWeaponsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LichSisterWeaponsService {

    ObjectMapper objectMapper;

    LichSisterWeaponsRepository repository;

    public LichSisterWeaponsService( ObjectMapper objectMapper,LichSisterWeaponsRepository repository) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Integer initLichSisterWeaponsData() {
        log.info("开始初始化赤毒/信条武器 数据……");
        List<LichSisterWeapons> lichWeapons = getLichWeapons();
        List<LichSisterWeapons> sisterWeapons = getSisterWeapons();
        if (lichWeapons != null && sisterWeapons != null) {
            repository.saveAllAndFlush(lichWeapons);
            repository.saveAllAndFlush(sisterWeapons);
            log.info("初始化赤毒/信条武器 数据完成，共{}条", lichWeapons.size() + sisterWeapons.size());
            return lichWeapons.size() + sisterWeapons.size();
        }
        return -1;
    }


    private List<LichSisterWeapons> getLichWeapons() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.warn("未获取到赤毒武器");
                    return null;
                }
                List<LichSisterWeapons> weapons = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    LichSisterWeapons weapon = buildLichSisterWeapons(itemNode);
                    if (weapon != null) {
                        weapons.add(weapon);
                    }
                }
                return weapons;
            } catch (Exception e) {
                log.error("解析赤毒武器数据失败", e);
                return null;
            }
        }
        return null;
    }

    private List<LichSisterWeapons> getSisterWeapons() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.warn("未获取到信条武器");
                    return null;
                }
                List<LichSisterWeapons> weapons = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    LichSisterWeapons weapon = buildLichSisterWeapons(itemNode);
                    if (weapon != null) {
                        weapons.add(weapon);
                    }
                }
                return weapons;
            } catch (Exception e) {
                log.error("解析信条武器数据失败", e);
                return null;
            }
        }
        return null;
    }

    private LichSisterWeapons buildLichSisterWeapons(JsonNode object) {
        try {
            String gameRef = object.has("gameRef") ? object.get("gameRef").asText() : null;
            String id = object.has("id") ? object.get("id").asText() : null;
            String slug = object.has("slug") ? object.get("slug").asText() : null;
            Integer reqMasteryRank = object.has("reqMasteryRank") ? object.get("reqMasteryRank").asInt() : null;
            if (StringUtils.isAnyBlank(id, slug, gameRef)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JsonNode i18nNode = object.has("i18n") ? object.get("i18n") : null;
            JsonNode zhHansNode = (i18nNode != null && i18nNode.has("zh-hans")) ? i18nNode.get("zh-hans") : null;
            String name = (zhHansNode != null && zhHansNode.has("name")) ? zhHansNode.get("name").asText() : slug;

            return new LichSisterWeapons()
                    .setReqMasteryRank(reqMasteryRank)
                    .setGameRef(gameRef)
                    .setId(id)
                    .setSlug(slug)
                    .setName(name)
                    .setIcon(object.has("icon") ? object.get("icon").asText() : null)
                    .setThumb(object.has("thumb") ? object.get("thumb").asText() : null);
        } catch (Exception e) {
            log.error("解析物品数据失败: {}", object, e);
            return null;
        }
    }

}
