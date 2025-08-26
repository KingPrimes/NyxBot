package com.nyx.bot.controller;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.SecurityUtils;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
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

    @GetMapping("/auth/info")
    public AjaxResult getInfo() {
        SysUser loginUser = SecurityUtils.getLoginUser();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("userInfo", Map.of("userName", loginUser.getUserName()));
        return ajax;
    }
}
