package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "relics")
public class Relics extends BaseEntity {
    // 遗物所包含的物品
    @OneToMany(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            targetEntity = RelicsRewards.class
    )
    @JoinColumn(
            name = "rewardsId",
            referencedColumnName = "relicsId",
            nullable = false
    )
    // JSON 管理端 用于双向链接，解决嵌套过度的问题
    @JsonManagedReference
    @JsonProperty("rewards")
    List<RelicsRewards> rewards;

    @Id
    @JsonProperty("_id")
    String relicsId;
    // 遗物名称
    @JsonProperty("relicName")
    String relicName;
    // 遗物精练状态
    @JsonProperty("state")
    String state;
    // 纪元
    @JsonProperty("tier")
    String tier;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("relicsId", relicsId)
                .append("relicName", relicName)
                .append("state", state)
                .append("tier", tier)
                .append("rewards", rewards)
                .toString();
    }
}
