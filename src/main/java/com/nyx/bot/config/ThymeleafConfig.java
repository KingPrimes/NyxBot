package com.nyx.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringResourceTemplateResolver springResourceTemplateResolver() {
        SpringResourceTemplateResolver srtr = new SpringResourceTemplateResolver();
        srtr.setPrefix("file:./nyxTemplates/");
        srtr.setSuffix(".html");
        srtr.setTemplateMode(TemplateMode.HTML);
        srtr.setCharacterEncoding("UTF-8");
        srtr.setOrder(1);
        srtr.setCacheable(false);
        srtr.setCheckExistence(true);
        return srtr;
    }

}
