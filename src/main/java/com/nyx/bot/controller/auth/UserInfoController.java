package com.nyx.bot.controller.auth;


import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.SecurityUtils;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 获取用户信息
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name="auth.user_info", description = "用户信息接口")
@SecurityRequirement(name="Bearer")
@RestController
@CrossOrigin
public class UserInfoController extends BaseController {
    @Operation(
            summary = "获取用户信息",
            description = "获取用户信息",
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
                                                                 "userInfo": {
                                                                        "userName": "admin"
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
    @GetMapping("/auth/info")
    public AjaxResult getInfo() {
        SysUser loginUser = SecurityUtils.getLoginUser();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("userInfo", Map.of("userName", loginUser.getUserName()));
        return ajax;
    }
}
