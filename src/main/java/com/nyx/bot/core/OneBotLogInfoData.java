package com.nyx.bot.core;

import com.nyx.bot.enums.PermissionsEnums;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class OneBotLogInfoData {
    Long botUid;
    Long userUid;
    Long groupUid;
    String rawMsg;
    String time;
    PermissionsEnums permissionsEnums;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("botUid", getBotUid())
                .append("userUid", getUserUid())
                .append("groupUid", getGroupUid())
                .append("rawMsg", getRawMsg())
                .append("time", getTime())
                .append("permissionsEnums", getPermissionsEnums())
                .toString();
    }
}
