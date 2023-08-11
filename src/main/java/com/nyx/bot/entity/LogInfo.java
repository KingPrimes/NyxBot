package com.nyx.bot.entity;

import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.PermissionsEnums;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 日志信息
 */
@Data
@Entity
@Table
public class LogInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    // 请求的模块
    String title;
    // 操作指令
    Codes codes;
    // 用户等级
    PermissionsEnums permissions;
    // 操作类型
    BusinessType businessType;
    // 机器人ID
    Long botUid;
    // 请求的群组
    Long groupUid;
    // 请求人
    Long userUid;
    // 原始消息
    String rawMsg;
    // 请求的Url
    String url;
    // 请求的方法 POST,GET
    String method;
    // 请求的方法体
    String requestMethod;
    // 执行时间
    Long runTime;
    // 请求的参数
    String param;
    // 返回的结果
    String result;
    // 执行状态
    Integer status;
    // 错误信息
    String errorMsg;
    // 日志时间
    Date logTime;
}
