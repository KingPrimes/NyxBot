package com.nyx.bot.modules.warframe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class RivenItemsService {

    RivenItemsRepository repository;

    public RivenItemsService(RivenItemsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Integer initRivenItemsData() {
        log.debug("开始初始化紫卡武器数据……");
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_RIVEN_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            JSONArray data = JSON.parseObject(body.body()).getJSONArray("data");
            if (data.isEmpty()) {
                log.error("未获取到Market紫卡武器数据");
                return -1;
            }
            // 2. 使用Stream流处理集合，代码更简洁
            List<RivenItems> items = data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildRivenItems) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
            log.debug("成功初始化紫卡武器数据，数量为：{}", items.size());
            return repository.saveAll(items).size();
        }
        return -1;
    }


    private RivenItems buildRivenItems(JSONObject object) {
        try {
            String id = object.getString("id");
            String slug = object.getString("slug");
            String gameRef = object.getString("gameRef");
            String group = object.getString("group");
            String rivenType = object.getString("rivenType");
            Double disposition = object.getDouble("disposition");
            Integer reqMasteryRank = object.getInteger("reqMasteryRank");
            if (StringUtils.isAnyBlank(id, slug, gameRef, group, disposition.toString(), reqMasteryRank.toString())) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JSONObject i18n = object.getJSONObject("i18n");
            JSONObject zhHansI18n = i18n != null ? i18n.getJSONObject("zh-hans") : null;
            String name = zhHansI18n != null ? zhHansI18n.getString("name") : object.getString("slug"); // 名称缺失时用slug兜底
            String icon = zhHansI18n != null ? zhHansI18n.getString("icon") : null;
            String thumb = zhHansI18n != null ? zhHansI18n.getString("thumb") : null;
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
        } catch (JSONException e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
