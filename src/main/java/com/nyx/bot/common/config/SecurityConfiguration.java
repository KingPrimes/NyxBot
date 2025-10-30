package com.nyx.bot.common.config;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.filter.JwtRequestFilter;
import com.nyx.bot.handler.AuthenticationEntryPointImpl;
import com.nyx.bot.handler.LogoutSuccessHandler;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${shiro.ws.server.url}")
    String shiro;

    @Value("${test.isTest}")
    Boolean test;

    @Resource
    private UserDetailsService userDetailService;
    @Resource
    private AuthenticationEntryPointImpl unauthorizedHandler;
    @Resource
    private LogoutSuccessHandler logoutSuccessHandler;
    @Resource
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 配置持久化Token
     */
    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        repository.setCreateTableOnStartup(false);
        return repository;
    }

    //设置密码加密类型
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //注册过滤器并配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用csrf
                .csrf(AbstractHttpConfigurer::disable)
                // 配置跨域资源共享
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置无状态的会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //配置拦截规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index",
                                "/static/**",
                                "/auth/login",
                                // 机器人链接接口
                                shiro,
                                "/v3/api-docs"
                        ).permitAll()
                        //其余请求路径都需要权限才可以访问
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(false)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler((req, res, e) -> {  // 权限不足
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write(JSON.toJSONString(AjaxResult.error("权限不足")));
                        })
                )
                //禁用默认的登录表单
                .formLogin(AbstractHttpConfigurer::disable)
                // 配置JWT过滤器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(!test); // 允许携带身份验证信息
        if (test) {
            configuration.addExposedHeader("test-token");
            configuration.addAllowedOrigin("*");
            configuration.addAllowedMethod("*");
        }
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setMaxAge(3600L); // 预检请求缓存时间
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}





