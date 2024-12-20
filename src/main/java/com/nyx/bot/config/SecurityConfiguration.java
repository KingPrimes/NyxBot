package com.nyx.bot.config;

import com.nyx.bot.filter.JwtRequestFilter;
import com.nyx.bot.handler.LoginHandler;
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
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final UserDetailsService userDetailService;

    @Value("${shiro.ws.server.url}")
    String shiro;


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
                                new AntPathRequestMatcher("/h2-console/**"),
                                new AntPathRequestMatcher("/img/**"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/nyx/**"),
                                new AntPathRequestMatcher("/static/**"),
                                new AntPathRequestMatcher("/private/**"),
                                new AntPathRequestMatcher("/api/**"),
                                new AntPathRequestMatcher(shiro)
                        ).permitAll()
                        .requestMatchers("/login").permitAll()
                        //其余请求路径都需要权限才可以访问
                        .anyRequest().authenticated())
                //禁用默认的登录表单
                .formLogin(AbstractHttpConfigurer::disable)
                // 配置JWT过滤器
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.accessDeniedHandler(new LoginHandler()).authenticationEntryPoint(new LoginHandler()))
//                .formLogin(conf -> {
//                    //登录页面
//                    conf
//                            .loginPage("/login")
//                            //登录接口
//                            .loginProcessingUrl("/login").permitAll()
//                            .successHandler(new LoginHandler())
//                            .failureHandler(new LoginHandler());
//                })

                //配置退出
//                .logout(out -> {
//                    out.logoutUrl("/logout");
//                    out.logoutSuccessUrl("/login");
//                })
//                .rememberMe(me -> {
//                    //设置记住我的 name 默认为 remember-me
//                    me.rememberMeParameter("remember");
//                    // 设置token
//                    me.tokenRepository(tokenRepository);
//                    // 设置token存活时常
//                    me.tokenValiditySeconds(3600 * 24 * 7);
//                    // 只能通过HTTPS请求
//                    me.useSecureCookie(false);
//
//                })
//                .passwordManagement(pass -> {
//                    pass.changePasswordPage("/password");
//                })

                //跨域设置，仅允许同路径下的 iframe 页面
//                .headers(headers -> {
//                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
//                })
                .build();
    }

    //设置密码加密方式
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
        configuration.setAllowedOrigins(List.of("*")); // 设置允许的前端域名
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 设置允许的HTTP方法
        configuration.setAllowedHeaders(List.of("*")); // 设置允许的请求头
        configuration.setAllowCredentials(true); // 允许携带身份验证信息
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 配置JWT过滤器（示例，需要根据你的应用实现）
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }
}





