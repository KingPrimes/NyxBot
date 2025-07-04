package com.nyx.bot.entity.warframe.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.entity.warframe.exprot.relicarcane.RelicRewards;
import com.nyx.bot.enums.RarityEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table
public class RelicArcane {
    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    String uniqueName;

    @JsonProperty("name")
    String name;

    @JsonProperty("codexSecret")
    Boolean codexSecret;

    @JsonProperty("description")
    String description;

    @JsonProperty("rarity")
    RarityEnum rarity;

    @OneToMany(mappedBy = "rewardName", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonProperty("relicRewards")
    List<RelicRewards> relicRewards;

    @Column(columnDefinition = "json")
    @JsonProperty("levelStats")
    String levelStats;


}
