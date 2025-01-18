package com.nyx.bot.config;

import com.nyx.bot.filter.JwtRequestFilter;
import com.nyx.bot.handler.AuthenticationEntryPointImpl;
import com.nyx.bot.handler.LogoutSuccessHandler;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
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
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/index"),
                                new AntPathRequestMatcher("/static/**"),
                                new AntPathRequestMatcher("/auth/login"),
                                // 用于生成图片的接口
                                new AntPathRequestMatcher("/api/**"),
                                // 用于生成图片的接口
                                new AntPathRequestMatcher("/private/**"),
                                // 机器人链接接口
                                new AntPathRequestMatcher(shiro)
                        ).permitAll()
                        //其余请求路径都需要权限才可以访问
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                //禁用默认的登录表单
                .formLogin(AbstractHttpConfigurer::disable)
                // 配置JWT过滤器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 验证身份
     */
    @Bean
    AuthenticationManager authenticationManager() {
        // 创建DaoAuthenticationProvider实例
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        // 设置用户详情服务
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        // 设置不抛出用户未找到异常
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        // 设置密码加密器
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        // 返回认证管理器
        return new ProviderManager(daoAuthenticationProvider);
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
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}





