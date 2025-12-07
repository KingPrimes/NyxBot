package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
 * Warframe 可交易物品列表
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.market.item", description = "Warframe 可交易物品列表")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {

    OrdersItemsRepository repository;

    public OrdersItemsController(OrdersItemsRepository repository) {
        this.repository = repository;
    }

    @Operation(
            summary = "获取可交易物品列表",
            description = "分页获取可交易物品数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "可交易物品查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrdersItems.class),
                            examples = {
                                    @ExampleObject(value = """
                                            {"name":""}
                                            """)
                            }
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TableDataInfo.class)
                            )}
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody OrdersItems oi) {
        return getDataTable(
                repository.findAllPageable(
                        oi.getName(),
                        PageRequest.of(
                                oi.getCurrent() - 1, oi.getSize()
                        )
                )
        );
    }

    @Operation(
            summary = "更新可交易物品数据",
            description = "异步更新可交易物品相关数据",
            method = HttpMethod.POST,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "更新任务已启动",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::initOrdersItemsData);
        return success(I18nUtils.RequestTaskRun());
    }
}
