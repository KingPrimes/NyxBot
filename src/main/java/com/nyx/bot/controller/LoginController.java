package com.nyx.bot.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.utils.CacheUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录
 */
@RestController
@CrossOrigin
public class LoginController extends BaseController {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 10;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/login")
    public ApiResponse<?> login(@Validated @RequestBody SysUser authRequest, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String attemptKey = "login_attempt:" + clientIp;
        String lockKey = "login_lock:" + clientIp;

        // 检查是否已被锁定
        if (CacheUtils.exists(CacheUtils.SYSTEM, lockKey)) {
            return error("登录尝试过于频繁，请10分钟后再试");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 直接从 Authentication 获取 UserDetails，避免重复 DB 查询
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            return success("登录成功", Map.of("token", jwtUtil.generateToken(userDetails.getUsername())));
        } catch (UsernameNotFoundException e) {
            incrementFailedAttempts(clientIp, attemptKey, lockKey);
            return error(e.getMessage());
        } catch (Exception e) {
            incrementFailedAttempts(clientIp, attemptKey, lockKey);
            throw e;
        }
    }

    private void incrementFailedAttempts(String clientIp, String attemptKey, String lockKey) {
        Integer attempts = CacheUtils.get(CacheUtils.SYSTEM, attemptKey, Integer.class);
        int count = (attempts == null) ? 1 : attempts + 1;
        CacheUtils.putWithExpiry(CacheUtils.SYSTEM, attemptKey, count, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        if (count >= MAX_LOGIN_ATTEMPTS) {
            CacheUtils.putWithExpiry(CacheUtils.SYSTEM, lockKey, true, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }
}
