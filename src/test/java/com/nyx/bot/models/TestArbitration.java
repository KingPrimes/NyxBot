package com.nyx.bot.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.FileUtils;
import io.github.kingprimes.model.Arbitration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class TestArbitration {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    String file = FileUtils.readFileToString("./data/arbitration");
    List<Arbitration> arbitration;
    
    {
        try {
            arbitration = objectMapper.readValue(Base64.getDecoder().decode(file), new TypeReference<List<Arbitration>>() {});
        } catch (Exception e) {
            arbitration = List.of();
        }
    }

    @Test
    public void getArbitration() {

        long milli = ZonedDateTime.of(LocalDateTime.now(ZoneOffset.ofHours(8)), ZoneOffset.ofHours(8)).toInstant().getEpochSecond();
        Arbitration a = arbitration.stream()
                //过滤掉过期的数据
                .filter(ar -> ar.getExpiry().getEpochSecond() - milli > 0)
                //判断两个时间相差的毫秒数，并取最小值的元素
                .min(Comparator.comparingLong(obj -> obj.getExpiry().getEpochSecond() - milli))
                .orElse(null);
        log.info("Arbitration:{}", a);
    }
}
