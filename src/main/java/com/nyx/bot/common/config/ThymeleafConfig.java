package com.nyx.bot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringResourceTemplateResolver springResourceTemplateResolver() {
        SpringResourceTemplateResolver str = new SpringResourceTemplateResolver();
        str.setPrefix("file:./DataSource/Template/");
        str.setSuffix(".html");
        str.setTemplateMode(TemplateMode.HTML);
        str.setCharacterEncoding("UTF-8");
        str.setOrder(1);
        str.setCacheable(false);
        str.setCheckExistence(true);
        return str;
    }

}
