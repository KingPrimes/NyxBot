package com.nyx.bot.common.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafConfig {

    // 注入Spring Boot默认的模板解析器（优先级0，classpath:/templates/）
    @Resource
    private SpringResourceTemplateResolver springResourceTemplateResolver;

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

    // 配置支持双路径的模板引擎
    @Bean(name = "customTemplateEngine")
    public SpringTemplateEngine customTemplateEngine(@Qualifier("springResourceTemplateResolver")SpringResourceTemplateResolver customResourceTemplateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();

        // 添加默认解析器（优先级0）和自定义解析器（优先级1）
        // 模板引擎会按order从小到大依次查找模板
        engine.addTemplateResolver(springResourceTemplateResolver); // 默认路径（order=0）
        engine.addTemplateResolver(customResourceTemplateResolver); // 自定义路径（order=1）

        return engine;
    }

}
