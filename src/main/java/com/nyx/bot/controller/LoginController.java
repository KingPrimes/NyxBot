package com.nyx.bot.controller;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.SecurityUtils;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@CrossOrigin
public class LoginController extends BaseController {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserDetailsService userDetailsService;

    @PostMapping("/auth/login")
    @ApiOperation("登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authRequest", value = "登录请求参数", required = true, dataType = "SysUser", paramType = "body",
            examples = @Example(value = {
                    @ExampleProperty(mediaType = "userName", value = "admin"),
                    @ExampleProperty(mediaType = "password", value = "123456")
            })),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = AjaxResult.class,examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "登录成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            {
                                "token": "123456"
                            }
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
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

    @ApiOperation("获取用户信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = SysUser.class,examples = @Example(value = {
                    @ExampleProperty(mediaType = "userName", value = "admin"),
                    @ExampleProperty(mediaType = "password", value = "123456"),
                    @ExampleProperty(mediaType = "token", value = "123456"),
                    @ExampleProperty(mediaType = "userId", value = "1")
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/auth/info")
    public AjaxResult getInfo() {
        SysUser loginUser = SecurityUtils.getLoginUser();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("userInfo", Map.of("userName", loginUser.getUserName()));
        return ajax;
    }
}
