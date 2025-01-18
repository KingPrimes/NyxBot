package com.nyx.bot.filter;

import com.nyx.bot.core.Constants;
import com.nyx.bot.core.JwtUtil;
import com.nyx.bot.utils.CacheUtils;
import io.jsonwebtoken.ExpiredJwtException;
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

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain)
            throws ServletException, IOException {

        String username = null;
        String jwt = null;

        String requestHeader = request.getHeader("sec-websocket-protocol");
        String authorizationHeader = request.getHeader("authorization");
        if (requestHeader != null && !requestHeader.isEmpty()) {
            try {
                jwt = requestHeader;
                CacheUtils.set(CacheUtils.SYSTEM, "sec-websocket-protocol", requestHeader);
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                if (e instanceof ExpiredJwtException) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token已过期");
                }
                if (e instanceof IllegalArgumentException) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效Token");
                }
                return;
            }
        } else if (authorizationHeader != null && authorizationHeader.startsWith(Constants.TOKEN_PREFIX)) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                if (e instanceof ExpiredJwtException) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token已过期");
                }
                if (e instanceof IllegalArgumentException) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效Token");
                }
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}