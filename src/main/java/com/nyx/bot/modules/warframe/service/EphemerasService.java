package com.nyx.bot.modules.warframe.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.common.core.ApiUrl;
import com.nyx.bot.modules.warframe.entity.Ephemeras;
import com.nyx.bot.modules.warframe.repo.EphemerasRepository;
import com.nyx.bot.utils.StringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class EphemerasService {

    EphemerasRepository repository;

    public EphemerasService(EphemerasRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Integer initEphemerasData() {
        log.debug("开始初始化赤毒/信条幻纹 数据……");
        List<Ephemeras> lichEphemeras = getLichEphemeras();
        List<Ephemeras> sisterEphemeras = getSisterEphemeras();
        if (lichEphemeras != null && sisterEphemeras != null) {
            repository.saveAll(lichEphemeras);
            repository.saveAll(sisterEphemeras);
            log.debug("初始化赤毒/信条幻纹 数据完成，共{}条", lichEphemeras.size() + sisterEphemeras.size());
            return lichEphemeras.size() + sisterEphemeras.size();
        }
        return -1;
    }


    private List<Ephemeras> getLichEphemeras() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_LICH_EPHEMERAS);
        if (body.code().is2xxSuccessful()) {
            JSONArray data = JSON.parseObject(body.body()).getJSONArray("data");
            if (data.isEmpty()) {
                log.info("未获取到赤毒幻纹");
                return null;
            }

            return data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildEphemeras) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
        }
        return null;
    }


    private List<Ephemeras> getSisterEphemeras() {
        HttpUtils.Body body = HttpUtils.marketSendGet(ApiUrl.WARFRAME_MARKET_SISTER_EPHEMERAS);
        if (body.code().is2xxSuccessful()) {
            JSONArray data = JSON.parseObject(body.body()).getJSONArray("data");
            if (data.isEmpty()) {
                log.info("未获取到 sisters 幻纹");
                return null;
            }

            return data.stream()
                    .map(i -> (JSONObject) i) // 安全转换为JSONObject
                    .filter(Objects::nonNull) // 过滤null对象
                    .map(this::buildEphemeras) // 提取对象构建逻辑
                    .filter(Objects::nonNull) // 过滤构建失败的对象
                    .toList();
        }
        return null;
    }

    private Ephemeras buildEphemeras(JSONObject object) {
        try {
            String gameRef = object.getString("gameRef");
            String id = object.getString("id");
            String slug = object.getString("slug");
            if (StringUtils.isAnyBlank(id, slug)) {
                log.warn("物品关键信息缺失，跳过处理: {}", object);
                return null;
            }

            // 嵌套JSON安全解析
            JSONObject i18n = object.getJSONObject("i18n");
            JSONObject zhHansI18n = i18n != null ? i18n.getJSONObject("zh-hans") : null;
            String name = zhHansI18n != null ? zhHansI18n.getString("name") : object.getString("slug"); // 名称缺失时用slug兜底
            String icon = zhHansI18n != null ? zhHansI18n.getString("icon") : null;
            String thumb = zhHansI18n != null ? zhHansI18n.getString("thumb") : null;
            return new Ephemeras()
                    .setGameRef(gameRef)
                    .setId(id)
                    .setAnimation(object.getString("animation"))
                    .setElement(object.getString("element"))
                    .setSlug(slug)
                    .setName(name)
                    .setIcon(icon)
                    .setThumb(thumb);
        } catch (JSONException e) {
            log.error("解析物品数据失败: {}", object, e);
            return null; // 解析失败的物品跳过处理
        }
    }
}
