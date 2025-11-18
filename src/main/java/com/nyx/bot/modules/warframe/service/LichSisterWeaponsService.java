package com.nyx.bot.modules.warframe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.repo.LichSisterWeaponsRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class LichSisterWeaponsService {
    LichSisterWeaponsRepository repository;

    public LichSisterWeaponsService(LichSisterWeaponsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Integer initLichSisterWeaponsData() {
        log.debug("开始初始化赤毒/信条武器 数据……");
        List<LichSisterWeapons> lichWeapons = getLichWeapons();
        List<LichSisterWeapons> sisterWeapons = getSisterWeapons();
        if (lichWeapons != null && sisterWeapons != null) {
            repository.saveAll(lichWeapons);
            repository.saveAll(sisterWeapons);
            log.debug("初始化赤毒/信条武器 数据完成，共{}条", lichWeapons.size() + sisterWeapons.size());
            return lichWeapons.size() + sisterWeapons.size();
        }
        return -1;
    }


    private List<LichSisterWeapons> getLichWeapons() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_LICH_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            JSONArray data = JSON.parseObject(body.body()).getJSONArray("data");
            if (data.isEmpty()) {
                log.warn("未获取到赤毒武器");
                return null;
            }
            return data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildLichSisterWeapons) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
        }
        return null;
    }

    private List<LichSisterWeapons> getSisterWeapons() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SISTER_WEAPONS);
        if (body.code().is2xxSuccessful()) {
            JSONArray data = JSON.parseObject(body.body()).getJSONArray("data");
            if (data.isEmpty()) {
                log.warn("未获取到信条武器");
                return null;
            }
            return data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildLichSisterWeapons) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
        }
        return null;
    }

    private LichSisterWeapons buildLichSisterWeapons(JSONObject object) {
        try {
            String gameRef = object.getString("gameRef");
            String id = object.getString("id");
            String slug = object.getString("slug");
            Integer reqMasteryRank = object.getInteger("reqMasteryRank");
            if (StringUtils.isAnyBlank(id, slug, gameRef)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JSONObject i18n = object.getJSONObject("i18n");
            JSONObject zhHansI18n = i18n != null ? i18n.getJSONObject("zh-hans") : null;
            String name = zhHansI18n != null ? zhHansI18n.getString("name") : object.getString("slug"); // 名称缺失时用slug兜底

            return new LichSisterWeapons()
                    .setReqMasteryRank(reqMasteryRank)
                    .setGameRef(gameRef)
                    .setId(id)
                    .setSlug(slug)
                    .setName(name)
                    .setIcon(object.getString("icon"))
                    .setThumb(object.getString("thumb"));
        } catch (JSONException e) {
            log.error("解析物品数据失败: {}", object, e);
            return null;
        }
    }

}
