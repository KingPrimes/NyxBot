package com.nyx.bot.core;

import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.MarketFormEnums;
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
    //权限
    PermissionsEnums permissionsEnums;
    //指令
    Codes codes;

    //Market平台
    MarketFormEnums form;

    //key
    String key;

    //是否是买家
    Boolean isBy;

    //是否是满级
    Boolean isMax;

    //传输数据
    String data;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("botUid", botUid)
                .append("userUid", userUid)
                .append("groupUid", groupUid)
                .append("rawMsg", rawMsg)
                .append("time", time)
                .append("permissionsEnums", permissionsEnums)
                .append("codes", codes)
                .append("form", form)
                .append("key", key)
                .append("isBy", isBy)
                .append("isMax", isMax)
                .append("data", data)
                .toString();
    }
}
