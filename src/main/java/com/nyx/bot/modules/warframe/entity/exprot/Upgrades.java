package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table
public class Upgrades {

    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    String uniqueName;
    @JsonProperty("name")
    String name;
    @JsonProperty("polarity")
    String polarity;
    @JsonProperty("rarity")
    String rarity;
    @JsonProperty("codexSecret")
    Boolean codexSecret;
    @JsonProperty("baseDrain")
    Integer baseDrain;
    @JsonProperty("fusionLimit")
    Integer fusionLimit;
    @JsonProperty("compatName")
    String compatName;
    @JsonProperty("type")
    String type;
    @JsonProperty("tag")
    String tag;
    @JsonProperty("stats")
    String stats;
    @JsonProperty("modSet")
    String modSet;

}
