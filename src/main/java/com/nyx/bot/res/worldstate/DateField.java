package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.utils.TimeZoneUtil;
import lombok.Data;

@Data
public class DateField {
    @JsonProperty("$date")
    private D $date;

    public String getTime() {
        // 返回格式化之后的时间戳
        return TimeZoneUtil.formatTimestamp($date.getEpochSecond());
    }

    public Long getEpochSecond() {
        return $date.$numberLong;
    }

    @Data
    public static class D {
        private Long $numberLong;

        public Long getEpochSecond() {
            return $numberLong;
        }
    }
}
