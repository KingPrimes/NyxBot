package com.nyx.bot.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 预测仲裁
 */
@NoArgsConstructor
@Data
public class ArbitrationPre {

    /**
     * 时间
     */
    @JsonProperty("activation")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activation;
    /**
     * 结束时间
     */
    @JsonProperty("expiry")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiry;
    /**
     * 代号
     */
    @JsonProperty("id")
    private String id;
    /**
     * 节点
     */
    @JsonProperty("node")
    private String node;
    /**
     * 行星
     */
    @JsonProperty("planet")
    private String planet;
    /**
     * 敌人
     */
    @JsonProperty("enemy")
    private String enemy;
    /**
     * 任务类型
     */
    @JsonProperty("missionType")
    private String type;

}
