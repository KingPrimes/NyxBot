package com.nyx.bot.controller;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.JwtUtil;
import com.nyx.bot.core.SecurityUtils;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysUser;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class LoginController extends BaseController {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody SysUser authRequest) {
        AjaxResult ajax = AjaxResult.success();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUserName());

        return ajax.put("token", jwtUtil.generateToken(userDetails.getUsername()));
    }

    @GetMapping("/info")
    public AjaxResult getInfo() {
        SysUser loginUser = SecurityUtils.getLoginUser();
        AjaxResult ajax = AjaxResult.success();
        ajax.put("userName", loginUser.getUserName());
        return ajax;
    }
}
