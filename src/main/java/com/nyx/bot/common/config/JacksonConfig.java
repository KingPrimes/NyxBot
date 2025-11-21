package com.nyx.bot.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                // 解析时忽略未知的字段继续完成解析
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 忽略空对象
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 忽略枚举值转换错误
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                // 枚举值使用toString()输出
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false)
                // 禁用排序
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false)
                // 禁用排序
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false)
                // 添加反序列化时将单个值作为数组的配置
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                // 禁用反序列化时的纳秒级时间戳（避免将输入当作纳秒处理）
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                // 设置时区为UTC
                .defaultTimeZone(TimeZone.getTimeZone("UTC"))
                // 添加Java 8日期时间模块支持
                .addModule(new JavaTimeModule())
                .build();
    }
}