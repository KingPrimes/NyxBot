package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {

    @GetMapping
    @ApiOperation("获取配置")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = AjaxResult.class,examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            {
                                "serverPort": 8080,
                                "isServerOrClient": true,
                                "wsClientUrl": "ws://localhost:3001",
                                "wsServerUrl": "/ws/shiro"
                            }
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public AjaxResult loading() {
        return success().put("data", HandOff.getConfig());
    }


    @ApiOperation("保存配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "config", value = "配置信息", required = true, dataType = "NyxConfig", paramType = "body",
            examples = @Example(value = {
                   @ExampleProperty(mediaType = "serverPort",value = "8080"),
                   @ExampleProperty(mediaType = "isServerOrClient",value = "true"),
                   @ExampleProperty(mediaType = "wsClientUrl",value = "ws://localhost:3001"),
                   @ExampleProperty(mediaType = "wsServerUrl",value = "/ws/shiro"),
                   @ExampleProperty(mediaType = "token",value = "123456")
            })),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功"),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
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
