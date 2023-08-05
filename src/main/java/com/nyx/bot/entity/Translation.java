package com.nyx.bot.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 用于翻译Warframe 中英文
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"zhCn","usEn"}))
public class Translation {
    @Id
    //自增
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @JsonProperty("zh_cn")
    String zhCn;
    @JsonProperty("us_en")
    String usEn;
}
