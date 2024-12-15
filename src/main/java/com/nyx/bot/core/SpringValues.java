package com.nyx.bot.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class SpringValues {

    /**
     * 设置代理地址，如果未设置则用空值填入,默认为空，仅用于测试
     */
    @Value("${spring.sendgrid.proxy.host:}")
    public String proxyHost;

    /**
     * 设置代理地址端口，如果未设置则用空值填入,默认为空，仅用于测试
     */
    @Value("${spring.sendgrid.proxy.port:}")
    public String proxyPort;

}
