package com.nyx.bot.controller;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录
 */
@Tag(name = "auth.login", description = "登录接口")
@RestController
@CrossOrigin
public class LoginController extends BaseController {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserDetailsService userDetailsService;

    @Operation(
            summary = "登录",
            description = "登录",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "登录请求参数",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SysUser.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "userName":"name",
                                                        "password":"password"
                                                    }
                                                    """)
                                    }
                            )
                    }
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功",
                            content = {
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {@ExampleObject(value = """
                                                    {
                                                        "code": 200,
                                                        "msg": "登录成功",
                                                        "data": {
                                                            "token": "123456"
                                                        }
                                                    }
                                                    """)}
                                    )
                            }
                    )
            }
    )
    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody SysUser authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUserName());

            return success("登录成功", Map.of("token", jwtUtil.generateToken(userDetails.getUsername())));
        } catch (UsernameNotFoundException e) {
            return error(e.getMessage());
        }
    }
}
