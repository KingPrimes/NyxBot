package com.nyx.bot.service.log;

import com.nyx.bot.common.core.dao.LogInfoWebSocketDto;
import com.nyx.bot.common.event.LogEvent;
import org.springframework.stereotype.Component;

@Component
public class LogInfoMapper {
    public LogInfoWebSocketDto toDto(LogEvent event) {
        LogInfoWebSocketDto dto = new LogInfoWebSocketDto();
        dto.setLive(event.getLevel());
        dto.setTime(event.getTime());
        dto.setThread(event.getThread());
        dto.setPack(event.getPack());
        dto.setLog(event.getLog());
        return dto;
    }
}
