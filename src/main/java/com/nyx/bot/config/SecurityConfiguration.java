package com.nyx.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration  {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth ->{
                    auth.anyRequest().authenticated();
                })
                .formLogin(conf->{
                    conf.loginPage("/login");
                    conf.loginProcessingUrl("/login");
                    conf.defaultSuccessUrl("/");
                    conf.permitAll();
                })
                .build();
    }

}





