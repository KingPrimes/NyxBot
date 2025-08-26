package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "relics")
@NoArgsConstructor
@Data
public class Relics extends BaseEntity {

    @Id
    @JsonProperty("uniqueName")
    @Column(unique = true, nullable = false)
    private String uniqueName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("codexSecret")
    private Boolean codexSecret;

    @JsonProperty("description")
    private String description;

    @JsonProperty("relicRewards")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JoinColumn(name = "relics_id") // 外键列将在relic_rewards表中
    private List<RelicRewards> relicRewards;
}