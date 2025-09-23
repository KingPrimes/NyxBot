package com.nyx.bot.modules.system.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import com.nyx.bot.enums.LogTitleEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
@JsonView(Views.View.class)
public class LogInfo extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LogTitleEnum title;

    // 操作指令
    String code;
    // 用户等级
    String permissions;
    // 操作类型
    String businessType;
    // 机器人ID
    Long botUid;
    // 请求的群组
    Long groupUid;
    // 请求人
    Long userUid;
    // 原始消息
    @Column(columnDefinition = "text")
    String rawMsg;
    // 请求的Url
    @Column(columnDefinition = "text")
    String url;
    // 请求的方法 POST,GET
    String method;
    // 请求的方法体
    String requestMethod;
    // 执行时间
    Long runTime;
    // 请求的参数
    @Column(columnDefinition = "text")
    String param;
    // 返回的结果
    @Column(columnDefinition = "text")
    String result;
    // 执行状态
    Integer status;
    // 错误信息
    @Column(columnDefinition = "longtext")
    String errorMsg;
    // 日志时间
    Date logTime;
}
