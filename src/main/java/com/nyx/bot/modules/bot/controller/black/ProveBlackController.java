package com.nyx.bot.modules.bot.controller.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.entity.black.ProveBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人黑名单
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.bot.black.prove", description = "个人黑名单接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/bot/black/prove")
public class ProveBlackController extends BaseController {

    private final BlackService bs;

    public ProveBlackController(BlackService bs) {
        this.bs = bs;
    }

    @Operation(
            summary = "获取个人黑名单列表",
            description = "获取个人黑名单列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProveBlack.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "botUid": 123456,
                                                                "proveUid": 123456
                                                            }
                                                            """
                                            )
                                    }
                            )
                    }
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = TableDataInfo.class),
                                            examples = {
                                                    @ExampleObject(
                                                            value = """
                                                                    {
                                                                        "total": 1,
                                                                        "size": 10,
                                                                        "current": 1,
                                                                        "records": [
                                                                            {
                                                                                "id": 1,
                                                                                "botUid": 123456,
                                                                                "proveUid": 123456,
                                                                            }
                                                                        ]
                                                                    }
                                                                    """
                                                    )
                                            }
                                    )
                            }
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody ProveBlack pb) {
        return getDataTable(bs.list(pb));
    }

    @Operation(
            summary = "添加个人黑名单",
            description = "添加个人黑名单",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProveBlack.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "botUid": 123456,
                                                                "proveUid": 123456
                                                            }
                                                            """
                                            )
                                    }
                            )
                    }
            )
    )
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody ProveBlack pb) {
        return toAjax(bs.save(pb));
    }


    @Operation(
            summary = "删除个人黑名单",
            description = "删除个人黑名单",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "个人黑名单id",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class),
                            example = "123456"
                    )
            }
    )
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.removeProve(id));
    }

}
