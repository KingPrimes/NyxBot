package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.http.HttpUtils;
import io.github.kingprimes.model.Ducats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.github.kingprimes.model.Ducats.DumpType.DAY;
import static io.github.kingprimes.model.Ducats.DumpType.HOUR;

@Slf4j
@Component
public class MarketDucatsUtils {


    private final ObjectMapper objectMapper;

    private final OrdersItemsRepository ordersItemsRepository;

    public MarketDucatsUtils(ObjectMapper objectMapper, OrdersItemsRepository ordersItemsRepository) {
        this.objectMapper = objectMapper;
        this.ordersItemsRepository = ordersItemsRepository;
    }

    /**
     * 获取杜卡德币数据
     *
     * @return 返回解析后的杜卡德币数据对象，如果获取失败则返回null
     */
    public Ducats getDucats() {
        HttpUtils.Body body = HttpUtils.marketSendGet("https://api.warframe.market/v1/tools/ducats", "");
        if (!body.code().is2xxSuccessful()) {
            log.debug("获取Ducats数据失败：{}", body.body());
            return null;
        }
        try {
            return objectMapper.readValue(body.body(), Ducats.class);
        } catch (Exception e) {
            log.error("解析Ducats数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据指定类型获取杜卡德金币数据
     *
     * @param type   杜卡德币类型（银币或金币）
     * @param ducats 包含小时和日数据的杜卡德币对象
     * @return 返回包含小时和日数据的映射表，键为时间类型，值为杜卡德币列表；如果类型不匹配则返回null
     */
    public Map<Ducats.DumpType, List<Ducats.Ducat>> getDuats(DucatsType type, Ducats ducats) {
        Map<Ducats.DumpType, List<Ducats.Ducat>> dumpTypeListMap = new java.util.HashMap<>();
        List<Ducats.Ducat> previousDay = ducats.getPayload().getPreviousDay();
        List<Ducats.Ducat> previousHour = ducats.getPayload().getPreviousHour();
        if (type.equals(DucatsType.SILVER)) {
            dumpTypeListMap.put(DAY, getSilverDump(previousDay));
            dumpTypeListMap.put(HOUR, getSilverDump(previousHour));
            return dumpTypeListMap;

        } else if (type.equals(DucatsType.GOD)) {
            dumpTypeListMap.put(DAY, getGodDump(previousDay));
            dumpTypeListMap.put(HOUR, getGodDump(previousHour));
            return dumpTypeListMap;
        }
        return null;
    }

    /**
     * 获取银币类型的杜卡德币数据
     *
     * @param ducats 杜卡德币列表
     * @return 返回经过筛选和排序后的杜卡德币列表，最多包含10个元素
     */
    @SuppressWarnings("null")
    private List<Ducats.Ducat> getSilverDump(List<Ducats.Ducat> ducats) {
        return ducats.stream().filter(ducat -> ducat.getDucats() >= 45 && ducat.getDucats() < 100)
                .peek(d -> d.setItem(ordersItemsRepository.findById(d.getItem()).orElse(new OrdersItems()).getName()))
                .sorted((o1, o2) -> o2.getDucatsPerPlatinumWa().compareTo(o1.getDucatsPerPlatinumWa()))
                .limit(10)
                .toList();
    }

    /**
     * 获取金币类型的杜卡德币数据
     *
     * @param ducats 杜卡德币列表
     * @return 返回经过筛选和排序后的杜卡德币列表
     */
    @SuppressWarnings("null")
    private List<Ducats.Ducat> getGodDump(List<Ducats.Ducat> ducats) {
        return ducats.stream().filter(ducat -> ducat.getDucats() == 100)
                .peek(d -> d.setItem(ordersItemsRepository.findById(d.getItem()).orElse(new OrdersItems()).getName()))
                .sorted((o1, o2) -> o2.getDucatsPerPlatinumWa().compareTo(o1.getDucatsPerPlatinumWa()))
                .limit(10)
                .toList();
    }

    public enum DucatsType {
        SILVER,
        GOD
    }
}
