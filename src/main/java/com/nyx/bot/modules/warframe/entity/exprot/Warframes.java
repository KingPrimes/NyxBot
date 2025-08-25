package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 战甲
 */
@NoArgsConstructor
@Data
@Entity
@Table
public class Warframes {

    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    private String uniqueName;
    @JsonProperty("name")
    private String name;
    @JsonProperty("parentName")
    private String parentName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("health")
    private Integer health;
    @JsonProperty("shield")
    private Integer shield;
    @JsonProperty("armor")
    private Integer armor;
    @JsonProperty("stamina")
    private Integer stamina;
    @JsonProperty("power")
    private Integer power;
    @JsonProperty("codexSecret")
    private Boolean codexSecret;
    @JsonProperty("masteryReq")
    private Integer masteryReq;
    @JsonProperty("sprintSpeed")
    private Integer sprintSpeed;
    @OneToMany(mappedBy = "abilityUniqueName", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonProperty("abilities")
    private List<Abilities> abilities;
    @JsonProperty("productCategory")
    private String productCategory;

    @NoArgsConstructor
    @Data
    @Entity
    @Table
    public static class Abilities {
        @Id
        @NotEmpty(message = "unique_name.not.empty")
        @JsonProperty("abilityUniqueName")
        private String abilityUniqueName;
        @JsonProperty("abilityName")
        private String abilityName;
        @JsonProperty("description")
        private String description;
    }
}
