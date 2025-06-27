package com.nyx.bot.res.worldstate;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Event extends BastWorldState{
    private List<Message> messages;
    private String prop;
    private String icon;
    private Boolean priority;
    private Boolean mobileOnly;
    private Boolean community;
    // 添加字段映射
    private DateField date;
    private String imageUrl;
    private DateField eventStartDate;
    private DateField eventEndDate;
    private Boolean hideEndDateModifier;

}
