package com.nyx.bot.handler;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.ServletUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 认证失败处理类
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpCodeEnum.UNAUTHORIZED, msg)));
    }
}
