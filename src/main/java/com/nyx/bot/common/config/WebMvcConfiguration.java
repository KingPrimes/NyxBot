package com.nyx.bot.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    /**
     * 静态资源匹配
     */
    @Override
    protected void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/public/");
        super.addResourceHandlers(registry);
    }

    @NonNull
    @Override
    public LocaleResolver localeResolver() {
        super.localeResolver();
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.CHINA);
        return resolver;

    }
}
