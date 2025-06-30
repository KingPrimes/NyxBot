package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.utils.TimeUtils;
import com.nyx.bot.utils.TimeZoneUtil;
import lombok.Data;

import java.time.Instant;

@Data
public class DateField {
    @JsonProperty("$date")
    private D date;

    public String getTime() {
        // 返回格式化之后的时间戳
        return TimeZoneUtil.formatTimestamp(date.getEpochSecond().toEpochMilli());
    }

    public String getTimeLeft() {
        return TimeUtils.timeDeltaToString(date.getEpochSecond().toEpochMilli() - System.currentTimeMillis());
    }

    public Instant getEpochSecond() {
        return date.getEpochSecond();
    }

    @Data
    public static class D {
        @JsonProperty("$numberLong")
        private Instant numberLong;

        public Instant getEpochSecond() {
            return numberLong;
        }
    }
}
