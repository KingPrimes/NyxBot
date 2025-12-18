package com.nyx.bot.modules.warframe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RivenItemsService {

    private final ObjectMapper objectMapper;

    private final RivenItemsRepository repository;

    public RivenItemsService(ObjectMapper objectMapper,RivenItemsRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Transactional
    public Integer initRivenItemsData() {
        log.info("开始初始化紫卡武器数据……");
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_RIVEN_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            try {
                JsonNode rootNode = objectMapper.readTree(body.body());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode == null || !dataNode.isArray() || dataNode.isEmpty()) {
                    log.error("未获取到Market紫卡武器数据");
                    return -1;
                }
                // 使用Stream流处理集合，代码更简洁
                List<RivenItems> items = new ArrayList<>();
                for (JsonNode itemNode : dataNode) {
                    RivenItems item = buildRivenItems(itemNode);
                    if (item != null) {
                        items.add(item);
                    }
                }
                log.info("成功初始化紫卡武器数据，数量为：{}", items.size());
                return repository.saveAllAndFlush(items).size();
            } catch (Exception e) {
                log.error("解析紫卡武器数据失败", e);
                return -1;
            }
        }
        return -1;
    }


    private RivenItems buildRivenItems(JsonNode object) {
        try {
            String id = object.has("id") ? object.get("id").asText() : null;
            String slug = object.has("slug") ? object.get("slug").asText() : null;
            String gameRef = object.has("gameRef") ? object.get("gameRef").asText() : null;
            String group = object.has("group") ? object.get("group").asText() : null;
            String rivenType = object.has("rivenType") ? object.get("rivenType").asText() : null;
            Double disposition = object.has("disposition") ? object.get("disposition").asDouble() : null;
            Integer reqMasteryRank = object.has("reqMasteryRank") ? object.get("reqMasteryRank").asInt() : null;
            
            if (StringUtils.isAnyBlank(id, slug, gameRef, group) || disposition == null || reqMasteryRank == null) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JsonNode i18nNode = object.has("i18n") ? object.get("i18n") : null;
            JsonNode zhHansNode = (i18nNode != null && i18nNode.has("zh-hans")) ? i18nNode.get("zh-hans") : null;
            String name = (zhHansNode != null && zhHansNode.has("name")) ? zhHansNode.get("name").asText() : slug;
            String icon = (zhHansNode != null && zhHansNode.has("icon")) ? zhHansNode.get("icon").asText() : null;
            String thumb = (zhHansNode != null && zhHansNode.has("thumb")) ? zhHansNode.get("thumb").asText() : null;
            
            return new RivenItems()
                    .setId(id)
                    .setSlug(slug)
                    .setGameRef(gameRef)
                    .setGroup(group)
                    .setRivenType(rivenType)
                    .setDisposition(disposition)
                    .setReqMasteryRank(reqMasteryRank)
                    .setName(name)
                    .setIcon(icon)
                    .setThumb(thumb);
        } catch (Exception e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
