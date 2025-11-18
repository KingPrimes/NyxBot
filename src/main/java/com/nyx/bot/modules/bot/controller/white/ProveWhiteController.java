package com.nyx.bot.modules.bot.controller.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.entity.white.ProveWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
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
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人白名单
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.bot.white.prove", description = "个人白名单")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {

    @Resource
    WhiteService whiteService;

    @Resource
    BlackService bs;

    @Operation(
            summary = "查询个人白名单列表",
            description = "查询个人白名单列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "个人白名单对象",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProveWhite.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "botUid": 123456,
                                                        "proveUid": 123456
                                                    }
                                                    """)
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
                                                    @ExampleObject(value = """
                                                            {
                                                                "total": 1,
                                                                "size": 10,
                                                                "current": 1,
                                                                "records": [
                                                                    {
                                                                        "id": 1,
                                                                        "botUid": 123456,
                                                                        "proveUid": 123456
                                                                    }
                                                                ]
                                                            }
                                                            """)
                                            }
                                    )
                            }
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody ProveWhite proveWhite) {
        return getDataTable(whiteService.list(proveWhite));
    }

    @Operation(
            summary = "添加个人白名单",
            description = "添加个人白名单",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "个人白名单对象",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProveWhite.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "botUid": 123456,
                                                        "proveUid": 123456
                                                    }
                                                    """)
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
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {
                                                    @ExampleObject(value = """
                                                            {
                                                                "code": 200,
                                                                "msg": "添加成功"
                                                            }
                                                            """)
                                            }
                                    )
                            }
                    )
            }
    )
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody ProveWhite white) {
        if (bs.isBlack(null, white.getProveUid())) {
            return toAjax(whiteService.save(white) != null);
        }
        return error(HttpStatus.BAD_REQUEST, I18nUtils.BWBlackExist());
    }

    @Operation(
            summary = "删除个人白名单",
            description = "删除个人白名单",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "个人白名单ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class),
                            example = "123456"
                    )
            }
    )
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.removeProve(id);
        return success();
    }
}
