package com.nyx.bot.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nyx.bot.utils.FileUtils;
import io.github.kingprimes.model.Arbitration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

@Slf4j
public class TestArbitration {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
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
            // 忽略枚举值大小写
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            // 设置时区为UTC
            .defaultTimeZone(TimeZone.getTimeZone("UTC"))
            // 添加Java 8日期时间模块支持
            .addModule(new JavaTimeModule())
            .build();
    String file = FileUtils.readFileToString("./data/arbitration.json");
    List<Arbitration> arbitration;

    {
        try {
            arbitration = objectMapper.readValue(file, new TypeReference<List<Arbitration>>() {
            });
        } catch (Exception e) {
            arbitration = List.of();
        }
    }

    @Test
    public void getArbitration() {
        //log.info(arbitration.toString());
        long milli = Instant.now().getEpochSecond();
        Arbitration a = arbitration.stream()
                //过滤掉过期的数据
                .filter(ar -> ar.getExpiry().getEpochSecond() - milli > 0)
                //判断两个时间相差的毫秒数，并取最小值的元素
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getEpochSecond() - milli))
                .orElse(null);
        log.info("Arbitration:{}", a);
    }

    @Test
    public void getArbitrationList() throws JsonProcessingException {
        // 获取当前时间（北京时间）
        long currentMilli = Instant.now().getEpochSecond();

        // 第一次尝试获取
        List<Arbitration> result = arbitration.stream()
                .filter(Arbitration::isWorth)
                .filter(ar -> ar.getActivation().getEpochSecond() > currentMilli)
                .limit(10)
                .toList();

        log.info("ArbitrationList:{}", objectMapper.writeValueAsString(result));
    }
}
