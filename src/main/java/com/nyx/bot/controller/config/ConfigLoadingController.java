package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
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
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.system", description = "重置密码")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {


    @Operation(
            summary = "获取配置",
            description = "获取当前配置",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {
                                                    @ExampleObject(value = """
                                                            {
                                                                "code": 200,
                                                                "msg": "获取成功",
                                                                "data": {
                                                                    "serverPort": 8080,
                                                                    "isServerOrClient": true,
                                                                    "wsClientUrl": "ws://localhost:3001",
                                                                    "wsServerUrl": "/ws/shiro"
                                                                }
                                                            }
                                                            """
                                                    )
                                            }
                                    )
                            }
                    )
            }
    )
    @GetMapping
    public AjaxResult loading() {
        return success().put("data", HandOff.getConfig());
    }


    @Operation(
            summary = "保存配置",
            description = "保存当前配置",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "配置信息",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = NyxConfig.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "serverPort": 8080,
                                                        "isServerOrClient": true,
                                                        "wsClientUrl": "ws://localhost:3001",
                                                        "wsServerUrl": "/ws/shiro",
                                                        "token": "123456"
                                                    }
                                                    """
                                            )
                                    }
                            )
                    }
            )
    )
    @PostMapping
    public AjaxResult save(@Validated @RequestBody NyxConfig config) {
        if (!config.isValidateServerUrl()) {
            return error(I18nUtils.RequestValidServerUrl());
        }
        if (!config.isValidateClientUrl()) {
            return error(I18nUtils.RequestValidClientUrl());
        }
        return toAjax(HandOff.handoff(config));
    }

}
