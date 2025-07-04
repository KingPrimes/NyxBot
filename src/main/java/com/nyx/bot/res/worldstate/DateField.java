package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.utils.TimeUtils;
import com.nyx.bot.utils.TimeZoneUtil;
import lombok.Data;

import java.time.Instant;

@Data
public class DateField {
    @JsonProperty("$date")
    private D date;

    @JsonIgnore
    public String getTime() {
        // 返回格式化之后的时间戳
        return TimeZoneUtil.formatTimestamp(date.getEpochSecond().toEpochMilli());
    }

    @JsonIgnore
    public String getTimeLeft() {
        return TimeUtils.timeDeltaToString(date.getEpochSecond().toEpochMilli() - System.currentTimeMillis());
    }

    @JsonIgnore
    public Instant getEpochSecond() {
        return date.getEpochSecond();
    }

    @Data
    public static class D {
        @JsonProperty("$numberLong")
        private Instant numberLong;

        @JsonIgnore
        public Instant getEpochSecond() {
            return numberLong;
        }
    }
}
