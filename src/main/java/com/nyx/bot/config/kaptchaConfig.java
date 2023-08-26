package com.nyx.bot.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static com.google.code.kaptcha.Constants.*;

/**
 * 配置验证码
 */
@Configuration
public class kaptchaConfig {


    @Bean
    public DefaultKaptcha kaptcha(){
        DefaultKaptcha dk = new DefaultKaptcha();
        Properties p = new Properties();

        // 是否有边框
        p.setProperty(KAPTCHA_BORDER,"yes");
        // 设置验证码文本字符颜色
        p.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR,"black");
        // 验证码图片宽度
        p.setProperty(KAPTCHA_IMAGE_WIDTH,"160");
        // 验证码高度
        p.setProperty(KAPTCHA_IMAGE_HEIGHT,"60");
        // 文本大小
        p.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE,"35");
        // Session key
        p.setProperty(KAPTCHA_SESSION_CONFIG_KEY,"code");
        // 验证码文本长度
        p.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH,"4");
        // 验证码文本字体样式
        p.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES,"Arial,Courier");
        // 图片样式
        p.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL,"com.google.code.kaptcha.impl.ShadowGimpy");

        dk.setConfig(new Config(p));

        return dk;
    }



}
