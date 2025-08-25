package com.nyx.bot.modules.warframe.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class Owner {
    /*声望*/
    @JsonProperty("reputation")
    private Integer reputation;
    /*区服*/
    @JsonProperty("locale")
    private String locale;
    /*玩家头像*/
    @JsonProperty("avatar")
    private String avatar;
    /*上次登录时间*/
    @JsonProperty("last_seen")
    private LocalDateTime lastSeen;
    /*游戏内名称*/
    @JsonProperty("ingame_name")
    private String ingameName;
    /*用户状态*/
    @JsonProperty("status")
    private String status;
    /*用户ID*/
    @JsonProperty("id")
    private String id;
    /*所使用的语言*/
    @JsonProperty("region")
    private String region;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("reputation", getReputation())
                .append("region", getRegion())
                .append("last_seen", getLastSeen())
                .append("ingame_name", getIngameName())
                .append("avatar", getAvatar())
                .append("status", getStatus())
                .append("id", getId())
                .toString();
    }
}
