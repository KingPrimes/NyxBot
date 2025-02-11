package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class TestMission {

    @Test
    public void testArbitration() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
        Date date = new Date(now.toEpochSecond(ZoneOffset.ofHours(8)) * 1000L);
        AtomicReference<ArbitrationPre> collect = new AtomicReference<>(new ArbitrationPre());
        ApiUrl.arbitrationPreList().stream()
                .peek(item -> {
                    System.out.println(StringUtils.format("item:{}", JSON.toJSONString(item)));
                    System.out.println(StringUtils.format("getDateSecond:{} --- second:{} --- Time:{}", DateUtils.getDateSecond(date, item.getExpiry()) > 0, DateUtils.getDateSecond(date, item.getExpiry()), item.getExpiry().getTime() - date.getTime()));
                })
                .min(Comparator.comparingLong(obj -> ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .ifPresentOrElse(collect::set, () -> {
                });
        System.out.println(StringUtils.format("collect:{}", JSON.toJSONString(collect.get())));
    }
}
