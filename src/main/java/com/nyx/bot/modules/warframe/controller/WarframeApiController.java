package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.enums.FissureTypeEnum;
import com.nyx.bot.modules.warframe.utils.MarketDucatsUtils;
import com.nyx.bot.modules.warframe.utils.MarketOrderUtils;
import com.nyx.bot.modules.warframe.utils.MarketRivenUtils;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.enums.FactionEnum;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.VoidEnum;
import io.github.kingprimes.model.worldstate.ActiveMission;
import io.github.kingprimes.model.worldstate.AllCycle;
import io.github.kingprimes.model.worldstate.Invasion;
import io.github.kingprimes.model.worldstate.SeasonInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Warframe API 接口
 * 提供给App使用的Warframe游戏数据查询接口
 */
@RestController
@RequestMapping("/api/warframe")
public class WarframeApiController extends BaseController {

    private final WorldStateUtils worldStateUtils;
    private final MarketRivenUtils marketRivenUtils;
    private final MarketOrderUtils marketOrderUtils;
    private final MarketDucatsUtils marketDucatsUtils;

    public WarframeApiController(
            WorldStateUtils worldStateUtils,
            MarketRivenUtils marketRivenUtils,
            MarketOrderUtils marketOrderUtils,
            MarketDucatsUtils marketDucatsUtils
    ) {
        this.worldStateUtils = worldStateUtils;
        this.marketRivenUtils = marketRivenUtils;
        this.marketOrderUtils = marketOrderUtils;
        this.marketDucatsUtils = marketDucatsUtils;
    }

    /**
     * 获取平原时间信息
     * 包含地球夜昼循环、Cetus、Vallis、Cambion、Zariman等平原时间
     */
    @GetMapping("/cycle")
    public ApiResponse<?> getCycle() {
        try {
            AllCycle cycle = worldStateUtils.getAllCycle();
            return success(cycle);
        } catch (DataNotInfoException e) {
            return error("获取平原时间失败: " + e.getMessage());
        }
    }

    /**
     * 获取入侵信息
     */
    @GetMapping("/invasions")
    public ApiResponse<?> getInvasions() {
        try {
            List<Invasion> invasions = worldStateUtils.getInvasions();
            return success(invasions);
        } catch (DataNotInfoException e) {
            return error("获取入侵信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取裂隙信息
     *
     * @param type 裂隙类型：ACTIVE_MISSION(普通)、STEEL_PATH(钢铁之路)、VOID_STORMS(虚空风暴)
     */
    @GetMapping("/fissures")
    public ApiResponse<?> getFissures(@RequestParam(defaultValue = "ACTIVE_MISSION") String type
    ) {
        try {
            FissureTypeEnum fissureType = FissureTypeEnum.valueOf(type.toUpperCase());
            List<ActiveMission> fissures = worldStateUtils.getFissure(fissureType);
            return success(fissures);
        } catch (IllegalArgumentException e) {
            return error("无效的裂隙类型，支持类型：ACTIVE_MISSION, STEEL_PATH, VOID_STORMS");
        } catch (DataNotInfoException e) {
            return error("获取裂隙信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取电波信息（Nightwave）
     */
    @GetMapping("/nightwave")
    public ApiResponse<?> getNightwave() {
        try {
            SeasonInfo seasonInfo = worldStateUtils.getSeasonInfo();
            return success(seasonInfo);
        } catch (DataNotInfoException e) {
            return error("获取电波信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取突击信息
     */
    @GetMapping("/sorties")
    public ApiResponse<?> getSorties() {
        try {
            var sorties = worldStateUtils.getSorties();
            return success(sorties);
        } catch (DataNotInfoException e) {
            return error("获取突击信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取执刑官猎杀信息
     */
    @GetMapping("/archon")
    public ApiResponse<?> getArchonHunt() {
        try {
            var archonHunt = worldStateUtils.getLiteSorite();
            return success(archonHunt);
        } catch (DataNotInfoException e) {
            return error("获取执刑官猎杀失败: " + e.getMessage());
        }
    }

    /**
     * WM查询 - 紫卡市场查询
     *
     * @param keyword 紫卡武器名称
     */
    @GetMapping("/wm/riven")
    public ApiResponse<?> getRivenMarket(@RequestParam String keyword
    ) {
        try {
            var result = marketRivenUtils.marketRivenParameter(keyword);
            return success(result);
        } catch (RuntimeException e) {
            return error("查询紫卡市场失败: " + e.getMessage());
        }
    }

    /**
     * WM查询 - 市场订单查询
     *
     * @param keyword  物品名称
     * @param platform 平台：PC, XB1, PS4, SWITCH, SWI
     */
    @GetMapping("/wm/order")
    public ApiResponse<?> getMarketOrder(@RequestParam String keyword, @RequestParam(defaultValue = "PC") String platform
    ) {
        try {
            MarketPlatformEnum platformEnum = MarketPlatformEnum.valueOf(platform.toUpperCase());
            MarketResult<OrdersItems, ?> result = marketOrderUtils.toSet(keyword, platformEnum);
            return success(result);
        } catch (IllegalArgumentException e) {
            return error("无效的平台类型，支持平台：PC, XB1, PS4, SWITCH, SWI");
        } catch (RuntimeException e) {
            return error("查询市场订单失败: " + e.getMessage());
        }
    }

    /**
     * WM查询 - 杜卡德币查询
     */
    @GetMapping("/wm/ducats")
    public ApiResponse<?> getDucats() {
        try {
            var ducats = marketDucatsUtils.getDucats();
            return success(ducats);
        } catch (RuntimeException e) {
            return error("查询杜卡德币失败: " + e.getMessage());
        }
    }

    /**
     * 获取警报信息
     */
    @GetMapping("/alerts")
    public ApiResponse<?> getAlerts() {
        try {
            var alerts = worldStateUtils.getAlerts();
            return success(alerts);
        } catch (DataNotInfoException e) {
            return error("获取警报信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取每日特惠信息
     */
    @GetMapping("/dailydeals")
    public ApiResponse<?> getDailyDeals() {
        try {
            var dailyDeals = worldStateUtils.getDailyDeals();
            return success(dailyDeals);
        } catch (DataNotInfoException e) {
            return error("获取每日特惠失败: " + e.getMessage());
        }
    }

    /**
     * 获取虚空商人信息
     */
    @GetMapping("/voidtrader")
    public ApiResponse<?> getVoidTrader() {
        try {
            var voidTrader = worldStateUtils.getVoidTraders();
            return success(voidTrader);
        } catch (DataNotInfoException e) {
            return error("获取虚空商人失败: " + e.getMessage());
        }
    }

    /**
     * 获取双衍王境轮换信息
     */
    @GetMapping("/duvalier")
    public ApiResponse<?> getDuvalierCycle() {
        try {
            var duvalierCycle = worldStateUtils.getDuvalierCycle();
            return success(duvalierCycle);
        } catch (DataNotInfoException e) {
            return error("获取双衍王境失败: " + e.getMessage());
        }
    }

    @GetMapping("/enums/mission-types")
    public ApiResponse<?> missionTypes() {
        return success(Arrays.stream(MissionTypeEnum.values())
                .collect(Collectors.toMap(MissionTypeEnum::name, MissionTypeEnum::getName)));
    }

    @GetMapping("/enums/factions")
    public ApiResponse<?> factions() {
        return success(Arrays.stream(FactionEnum.values())
                .collect(Collectors.toMap(FactionEnum::name, FactionEnum::getName)));
    }

    @GetMapping("/enums/void-tiers")
    public ApiResponse<?> voidTiers() {
        return success(Arrays.stream(VoidEnum.values())
                .collect(Collectors.toMap(VoidEnum::name, VoidEnum::getName)));
    }
}
