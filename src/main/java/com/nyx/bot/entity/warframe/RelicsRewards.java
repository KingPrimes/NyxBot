package com.nyx.bot.entity.warframe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


@Data
@Entity
@Table(name = "relicsRewards")
public class RelicsRewards {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JsonProperty("_id")
    String rewardId;
    // 物品名称
    @JsonProperty("itemName")
    String itemName;
    // 物品稀有度
    @JsonProperty("rarity")
    String rarity;
    // 获取概率
    @JsonProperty("chance")
    Integer chance;

    @ManyToOne(
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            targetEntity = Relics.class
    )
    @JoinColumn(name = "relics", referencedColumnName = "relicsId", nullable = false)
    // JSON 被管理端 用于双向链接，解决嵌套过度的问题
    @JsonBackReference
    Relics relics;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("rewardId", rewardId)
                .append("itemName", itemName)
                .append("rarity", rarity)
                .append("chance", chance)
                .append("relics", relics)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RelicsRewards that = (RelicsRewards) o;

        return new EqualsBuilder().append(id, that.id).append(rewardId, that.rewardId).append(itemName, that.itemName).append(rarity, that.rarity).append(chance, that.chance).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(rewardId).append(itemName).append(rarity).append(chance).toHashCode();
    }
}
