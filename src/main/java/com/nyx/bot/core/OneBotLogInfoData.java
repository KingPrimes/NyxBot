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

    public OneBotLogInfoData() {
    }

    public OneBotLogInfoData(Long botUid, Long userUid, Long groupUid, String rawMsg, String time, PermissionsEnums permissionsEnums, Codes codes, String data) {
        this.botUid = botUid;
        this.userUid = userUid;
        this.groupUid = groupUid;
        this.rawMsg = rawMsg;
        this.time = time;
        this.permissionsEnums = permissionsEnums;
        this.codes = codes;
        this.data = data;
    }

    public OneBotLogInfoData(Long botUid, Long userUid, Long groupUid, String rawMsg, String time, PermissionsEnums permissionsEnums, Codes codes, MarketFormEnums form, String key, Boolean isBy, Boolean isMax, String data) {
        this.botUid = botUid;
        this.userUid = userUid;
        this.groupUid = groupUid;
        this.rawMsg = rawMsg;
        this.time = time;
        this.permissionsEnums = permissionsEnums;
        this.codes = codes;
        this.form = form;
        this.key = key;
        this.isBy = isBy;
        this.isMax = isMax;
        this.data = data;
    }

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
