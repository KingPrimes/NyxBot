package com.nyx.bot.entity.warframe.exprot;

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
public class ModSet {

    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    private String uniqueName;

    @JsonProperty("numUpgradesInSet")
    private Integer numUpgradesInSet;

    @JsonProperty("buffSet")
    private Boolean buffSet;

    @JsonProperty("stats")
    private String stats;
}
