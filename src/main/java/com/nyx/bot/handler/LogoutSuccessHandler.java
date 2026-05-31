package com.nyx.bot.handler;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.ServletUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

import java.util.concurrent.TimeUnit;

/**
 * 登出成功处理类
 */
@Configuration
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 将当前令牌加入黑名单以使其失效
        revokeToken(request);
        ServletUtils.renderString(response, ApiResponse.ok(I18nUtils.message("user.logout.success")).toJsonString());
    }

    private void revokeToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("authorization");
            if (authHeader != null && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
                String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
                String jti = jwtUtil.extractJti(token);
                long remainingSeconds = jwtUtil.getRemainingExpirationSeconds(token);
                if (jti != null && remainingSeconds > 0) {
                    CacheUtils.putWithExpiry(CacheUtils.TOKEN_BLACKLIST, jti, true,
                            remainingSeconds, TimeUnit.SECONDS);
                }
            }
        } catch (Exception ignored) {
            // 令牌解析失败时静默处理，登出操作本身不受影响
        }
    }
}
