package com.nyx.bot.controller.auth;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.utils.CacheUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 刷新JWT令牌
 */
@RestController
public class RefreshTokenController extends BaseController {

    private final JwtUtil jwtUtil;

    public RefreshTokenController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/refreshToken")
    public ApiResponse<?> refreshToken(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return error("未能获取到Token");
        }

        String jti = jwtUtil.extractJti(token);
        if (jti != null && CacheUtils.exists(CacheUtils.TOKEN_BLACKLIST, jti)) {
            return error("Token已被撤销");
        }

        if (jwtUtil.isTokenExpired(token)) {
            return error("Token已过期，无法刷新，请重新登录");
        }

        String username = jwtUtil.extractUsername(token);
        String newToken = jwtUtil.generateToken(username);

        revokeToken(token);

        return success(Map.of("token", newToken, "refreshToken", newToken));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("authorization");
        if (authHeader != null && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            return authHeader.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    private void revokeToken(String token) {
        try {
            String jti = jwtUtil.extractJti(token);
            long remainingSeconds = jwtUtil.getRemainingExpirationSeconds(token);
            if (jti != null && remainingSeconds > 0) {
                CacheUtils.putWithExpiry(CacheUtils.TOKEN_BLACKLIST, jti, true,
                        remainingSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception ignored) {
        }
    }
}
