package com.nyx.bot.filter;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.utils.CacheUtils;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    // 定义不需要JWT验证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/static/",
            "/favicon"
    );
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {
        // 检查是否是需要排除的路径
        String requestURI = request.getRequestURI();
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestURI.contains(excludePath)) {
                chain.doFilter(request, response);
                return;
            }
        }
        try {
            String jwt = extractJwtToken(request);
            if (jwt != null) {
                String username = jwtUtil.extractUsername(jwt);

                // 如果用户已认证但上下文未设置，则设置认证信息
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            chain.doFilter(request, response);
        } catch (JwtException e) {
            if (e.getMessage().contains("过期")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已过期");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的Token: " + e.getMessage());
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证过程发生错误");
        }
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String extractJwtToken(HttpServletRequest request) {
        // 优先检查WebSocket协议头
        String webSocketProtocol = request.getHeader("sec-websocket-protocol");
        if (webSocketProtocol != null && !webSocketProtocol.isEmpty()) {
            CacheUtils.set(CacheUtils.SYSTEM, "sec-websocket-protocol", webSocketProtocol);
            return webSocketProtocol;
        }

        // 检查标准Authorization头
        String authorizationHeader = request.getHeader("authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(Constants.TOKEN_PREFIX)) {
            return authorizationHeader.substring(Constants.TOKEN_PREFIX.length());
        }

        return null;
    }

    /**
     * 发送JSON格式的错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(JSON.toJSONString(AjaxResult.error(message)));
    }
}