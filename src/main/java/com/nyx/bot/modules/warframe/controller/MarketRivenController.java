package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Warframe 紫卡武器
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.market_riven", description = "紫卡武器相关数据接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/market/riven")

public class MarketRivenController extends BaseController {
    private final RivenItemsRepository repository;

    private final WarframeDataSource dataSource;

    public MarketRivenController(RivenItemsRepository repository,WarframeDataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    @Operation(
            summary = "获取紫卡武器列表",
            description = "分页获取紫卡武器数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "紫卡武器查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RivenItems.class)
                    )}
            )
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody RivenItems rivenItems) {
        return getDataTable(
                repository.findAllPageable(
                        rivenItems.getName(),
                        rivenItems.getRivenType(),
                        PageRequest.of(
                                rivenItems.getCurrent() - 1,
                                rivenItems.getSize())
                )
        );
    }

    @Operation(
            summary = "更新紫卡武器数据",
            description = "异步更新紫卡武器相关数据",
            method = HttpMethod.POST
    )
    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(dataSource::getRivenWeapons);
        return success(I18nUtils.RequestTaskRun());
    }

}
