package com.nyx.bot.filter;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.utils.CacheUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Value("${shiro.ws.server.url}")
    private String shiro;

    // 定义不需要JWT验证的路径
    private List<String> EXCLUDE_PATHS;
    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        EXCLUDE_PATHS = Arrays.asList(
                "/static/",
                "/favicon",
                shiro);
    }

    /**
     * 允许异步分发时也执行此过滤器，确保 SSE 等异步请求的 SecurityContext 可用
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        // 检查是否是需要排除的路径
        String requestURI = request.getRequestURI();
        for (String excludePath : EXCLUDE_PATHS) {
            if (excludePath != null && requestURI.startsWith(excludePath)) {
                chain.doFilter(request, response);
                return;
            }
        }
        try {
            String jwt = extractJwtToken(request);
            if (jwt != null) {
                // 检查令牌是否已被撤销
                String jti = jwtUtil.extractJti(jwt);
                if (jti != null && CacheUtils.exists(CacheUtils.TOKEN_BLACKLIST, jti)) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已被撤销");
                    return;
                }

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
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已过期");
        } catch (JwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的Token");
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证过程发生错误");
        }
    }

    /**
     * 从请求中提取JWT令牌
     *
     * <p>提取优先级：WebSocket协议头 → Authorization头 → SSE URL参数(token)</p>
     */
    private String extractJwtToken(HttpServletRequest request) {
        // 1. WebSocket协议头（用于 WebSocket 连接认证）
        String webSocketProtocol = request.getHeader("sec-websocket-protocol");
        if (webSocketProtocol != null && !webSocketProtocol.isEmpty()) {
            CacheUtils.set(CacheUtils.SYSTEM, "sec-websocket-protocol", webSocketProtocol);
            return webSocketProtocol;
        }

        // 2. 标准 Authorization 头
        String authorizationHeader = request.getHeader("authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(Constants.TOKEN_PREFIX)) {
            return authorizationHeader.substring(Constants.TOKEN_PREFIX.length());
        }

        // 3. URL 查询参数 token（仅限 SSE 端点，浏览器 EventSource 不支持自定义请求头）
        if (request.getRequestURI().startsWith("/sse/")) {
            String tokenParam = request.getParameter("token");
            if (tokenParam != null && !tokenParam.isEmpty()) {
                return tokenParam;
            }
        }

        return null;
    }

    /**
     * 发送JSON格式的错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(status);
            response.getWriter().write(ApiResponse.error(status, message).toJsonString());
        } catch (Exception ignored) {
            // 响应已不可用（异步请求客户端断开等），无需处理
        }
    }
}