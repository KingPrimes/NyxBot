package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SocketGlobalStates {

    @JsonProperty("event")
    private String event;
    @JsonProperty("packet")
    private Packet packet;
    @JsonProperty("status")
    private int status;

    @Data
    public static class Packet {
        @JsonProperty("language")
        private String language;
        @JsonProperty("platform")
        private String platform;
        @JsonProperty("data")
        private GlobalStates data;
    }
}
