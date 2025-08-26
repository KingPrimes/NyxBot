package com.nyx.bot.handler;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;

/**
 * 登出成功处理类
 */
@Configuration
public class LogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(I18nUtils.message("user.logout.success"))));
    }
}
