package com.nyx.bot.common.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class SpringValues {
    @Value("${spring.sendgrid.proxy.url:}")
    public String url;

    @Value("${spring.sendgrid.proxy.username:}")
    public String username;
    @Value("${spring.sendgrid.proxy.password:}")
    public String password;


}
