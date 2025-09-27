package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@SuppressWarnings("unused")
@Data
@Accessors(chain = true)
public class BastWorldState {
    @JsonProperty("_id")
    private Id _id;

    // 开始时间
    @JsonProperty("Activation")
    private DateField activation;
    // 结束时间
    @JsonProperty("Expiry")
    private DateField expiry;

    @JsonIgnore
    public String getTimeLeft() {
        return expiry.getTimeLeft();
    }

    @Data
    public static class Id {
        @JsonProperty("$oid")
        private String oid;
    }
}
