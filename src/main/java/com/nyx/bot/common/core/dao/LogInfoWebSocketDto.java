package com.nyx.bot.common.core.dao;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 日志数据传输对象
 */
@Data
public class LogInfoWebSocketDto {
    String live;
    String time;
    String thread;
    String pack;
    String log;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("live", live)
                .append("time", time)
                .append("thread", thread)
                .append("pack", pack)
                .append("log", log)
                .toString();
    }
}
