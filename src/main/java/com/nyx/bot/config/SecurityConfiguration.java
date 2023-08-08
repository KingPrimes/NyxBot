package com.nyx.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatchers;

import javax.sql.DataSource;

@Configuration
public class SecurityConfiguration {

    @Value("${shiro.ws-config.ws-url}")
    String shiro;

    /**
     * 配置持久化Token
     */
    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource){
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    //注册过滤器并配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,PersistentTokenRepository repository) throws Exception {
        return http
                //配置拦截规则
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(
                                    RequestMatchers.anyOf(
                                            //放行静态资源
                                            request -> request.getRequestURI().contains("/static")
                                    )
                            )
                            .permitAll();
                    //其余请求路径都需要权限才可以访问
                    auth.anyRequest().authenticated();
                })
                //配置登录
                .formLogin(conf -> {
                    //登录页面
                    conf.loginPage("/login");
                    //登录接口
                    conf.loginProcessingUrl("/login");
                    //登录成功之后跳转的页面
                    conf.defaultSuccessUrl("/");
                    conf.permitAll();
                })
                //配置退出
                .logout(out -> {
                    out.logoutUrl("/logout");
                    out.logoutSuccessUrl("/login");
                })
                .rememberMe(me ->{
                    me.rememberMeParameter("remember");
                    me.tokenRepository(repository);
                    me.tokenValiditySeconds(3600 * 24 * 7);
                })
                // 禁用csrf
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * 配置Security WebSocket链接
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .requestMatchers(request ->
                                //匹配请求路径，如果请求路径相同则可以访问
                                request.getRequestURI().equals(shiro)
                        )
                ;
    }


  /*  @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        //daoAuthenticationProvider.setUserDetailsService(userService);
        return new ProviderManager(daoAuthenticationProvider);
    }*/

}





