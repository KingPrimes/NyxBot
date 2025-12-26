package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.enums.FissureTypeEnum;
import com.nyx.bot.modules.warframe.utils.MarketDucatsUtils;
import com.nyx.bot.modules.warframe.utils.MarketOrderUtils;
import com.nyx.bot.modules.warframe.utils.MarketRivenUtils;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import io.github.kingprimes.model.worldstate.ActiveMission;
import io.github.kingprimes.model.worldstate.AllCycle;
import io.github.kingprimes.model.worldstate.Invasion;
import io.github.kingprimes.model.worldstate.SeasonInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Warframe API 接口
 * 提供给App使用的Warframe游戏数据查询接口
 */
@Tag(name = "Warframe API", description = "Warframe游戏数据查询接口")
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
    @Operation(summary = "获取平原时间", description = "获取各个平原的时间循环信息")
    @GetMapping("/cycle")
    public AjaxResult getCycle() {
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
    @Operation(summary = "获取入侵信息", description = "获取当前的入侵任务列表")
    @GetMapping("/invasions")
    public AjaxResult getInvasions() {
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
    @Operation(summary = "获取裂隙信息", description = "获取裂隙任务列表，支持按类型筛选")
    @GetMapping("/fissures")
    public AjaxResult getFissures(
            @Parameter(description = "裂隙类型：ACTIVE_MISSION, STEEL_PATH, VOID_STORMS")
            @RequestParam(defaultValue = "ACTIVE_MISSION") String type
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
    @Operation(summary = "获取电波信息", description = "获取Nightwave电波任务信息")
    @GetMapping("/nightwave")
    public AjaxResult getNightwave() {
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
    @Operation(summary = "获取突击信息", description = "获取每日突击任务列表")
    @GetMapping("/sorties")
    public AjaxResult getSorties() {
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
    @Operation(summary = "获取执刑官猎杀", description = "获取执刑官猎杀任务信息")
    @GetMapping("/archon")
    public AjaxResult getArchonHunt() {
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
    @Operation(summary = "WM紫卡查询", description = "查询紫卡市场信息")
    @GetMapping("/wm/riven")
    public AjaxResult getRivenMarket(
            @Parameter(description = "紫卡武器名称关键词")
            @RequestParam String keyword
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
    @Operation(summary = "WM市场订单查询", description = "查询Warframe.Market物品订单信息")
    @GetMapping("/wm/order")
    public AjaxResult getMarketOrder(
            @Parameter(description = "物品名称关键词")
            @RequestParam String keyword,
            @Parameter(description = "平台：PC, XB1, PS4, SWITCH, SWI")
            @RequestParam(defaultValue = "PC") String platform
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
    @Operation(summary = "WM杜卡德币查询", description = "查询物品的杜卡德币兑换信息")
    @GetMapping("/wm/ducats")
    public AjaxResult getDucats() {
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
    @Operation(summary = "获取警报信息", description = "获取世界警报任务列表")
    @GetMapping("/alerts")
    public AjaxResult getAlerts() {
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
    @Operation(summary = "获取每日特惠", description = "获取虚空商人每日特惠信息")
    @GetMapping("/dailydeals")
    public AjaxResult getDailyDeals() {
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
    @Operation(summary = "获取虚空商人", description = "获取虚空商人位置和到达时间")
    @GetMapping("/voidtrader")
    public AjaxResult getVoidTrader() {
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
    @Operation(summary = "获取双衍王境", description = "获取双衍王境轮换信息")
    @GetMapping("/duvalier")
    public AjaxResult getDuvalierCycle() {
        try {
            var duvalierCycle = worldStateUtils.getDuvalierCycle();
            return success(duvalierCycle);
        } catch (DataNotInfoException e) {
            return error("获取双衍王境失败: " + e.getMessage());
        }
    }
}
