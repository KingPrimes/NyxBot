package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.Ephemeras;
import com.nyx.bot.modules.warframe.repo.EphemerasRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class EphemerasService {


    ObjectMapper objectMapper;

    EphemerasRepository repository;

    public EphemerasService(ObjectMapper objectMapper,EphemerasRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Transactional
    public Integer initEphemerasData() {
        log.info("开始初始化赤毒/信条幻纹 数据……");
        List<Ephemeras> lichEphemeras = getLichEphemeras();
        List<Ephemeras> sisterEphemeras = getSisterEphemeras();
        if (lichEphemeras != null && sisterEphemeras != null) {
            repository.saveAllAndFlush(lichEphemeras);
            repository.saveAllAndFlush(sisterEphemeras);
            log.info("初始化赤毒/信条幻纹 数据完成，共{}条", lichEphemeras.size() + sisterEphemeras.size());
            return lichEphemeras.size() + sisterEphemeras.size();
        }
        return -1;
    }


    private List<Ephemeras> getLichEphemeras() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.info("未获取到赤毒幻纹");
                    return null;
                }
                List<Ephemeras> ephemeras = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    Ephemeras ephemera = buildEphemeras(itemNode);
                    if (ephemera != null) {
                        ephemeras.add(ephemera);
                    }
                }
                return ephemeras;
            } catch (Exception e) {
                log.error("解析赤毒幻纹数据失败", e);
                return null;
            }
        }
        return null;
    }


    private List<Ephemeras> getSisterEphemeras() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SISTER_EPHEMERAS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.info("未获取到 sisters 幻纹");
                    return null;
                }
                List<Ephemeras> ephemeras = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    Ephemeras ephemera = buildEphemeras(itemNode);
                    if (ephemera != null) {
                        ephemeras.add(ephemera);
                    }
                }
                return ephemeras;
            } catch (Exception e) {
                log.error("解析 sisters 幻纹数据失败", e);
                return null;
            }
        }
        return null;
    }

    private Ephemeras buildEphemeras(JsonNode object) {
        try {
            String gameRef = object.has("gameRef") ? object.get("gameRef").asText() : null;
            String id = object.has("id") ? object.get("id").asText() : null;
            String slug = object.has("slug") ? object.get("slug").asText() : null;
            if (StringUtils.isAnyBlank(id, slug)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JsonNode i18nNode = object.has("i18n") ? object.get("i18n") : null;
            JsonNode zhHansNode = (i18nNode != null && i18nNode.has("zh-hans")) ? i18nNode.get("zh-hans") : null;
            String name = (zhHansNode != null && zhHansNode.has("name")) ? zhHansNode.get("name").asText() : slug;
            String icon = (zhHansNode != null && zhHansNode.has("icon")) ? zhHansNode.get("icon").asText() : null;
            String thumb = (zhHansNode != null && zhHansNode.has("thumb")) ? zhHansNode.get("thumb").asText() : null;
            return new Ephemeras()
                    .setGameRef(gameRef)
                    .setId(id)
                    .setAnimation(object.has("animation") ? object.get("animation").asText() : null)
                    .setElement(object.has("element") ? object.get("element").asText() : null)
                    .setSlug(slug)
                    .setName(name)
                    .setIcon(icon)
                    .setThumb(thumb);
        } catch (Exception e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
