package com.nyx.bot.common.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SpringValues {
    @Value("${spring.sendgrid.proxy.url:}")
    private String url;

    @Value("${spring.sendgrid.proxy.username:}")
    private String username;
    @Value("${spring.sendgrid.proxy.password:}")
    private String password;

}
