package com.nyx.bot.modules.warframe.entity.exprot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.modules.warframe.res.enums.FactionEnum;
import com.nyx.bot.modules.warframe.res.enums.MissionTypeEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@SuppressWarnings("unused")
@Data
@Entity
@Table
public class Nodes {

    @Id
    @NotEmpty(message = "unique_name.not.empty")
    @JsonProperty("uniqueName")
    String uniqueName;
    @NotEmpty(message = "state.name.not.empty")
    @JsonProperty("name")
    String name;

    @JsonProperty("systemName")
    String systemName;

    @JsonProperty("systemIndex")
    Integer systemIndex;

    @JsonProperty("nodeType")
    Integer nodeType;

    @JsonProperty("masteryReq")
    Integer masteryReq;

    @JsonProperty("missionIndex")
    Integer missionIndex;

    @JsonProperty("factionIndex")
    Integer factionIndex;

    @JsonProperty("minEnemyLevel")
    Integer minEnemyLevel;

    @JsonProperty("maxEnemyLevel")
    Integer maxEnemyLevel;


    @Transient
    @JsonIgnore
    public FactionEnum getFactionName() {
        switch (factionIndex) {
            case 0 -> {
                return FactionEnum.FC_GRINEER;
            }
            case 1 -> {
                return FactionEnum.FC_CORPUS;
            }
            case 2 -> {
                return FactionEnum.FC_INFESTATION;
            }
            case 3 -> {
                return FactionEnum.FC_OROKIN;
            }
            case 4 -> {
                return FactionEnum.FC_CORRUPTED;
            }
            case 5 -> {
                return FactionEnum.FC_SENTIENT;
            }
            case 6 -> {
                return FactionEnum.FC_NARMER;
            }
            case 7 -> {
                return FactionEnum.FC_MURMUR;
            }
            case 8 -> {
                return FactionEnum.FC_SCALDRA;
            }
            case 9 -> {
                return FactionEnum.FC_TECHROT;
            }
            case 10 -> {
                return FactionEnum.FC_DUVIRI;
            }
            case 11 -> {
                return FactionEnum.FC_MITW;
            }
            case 12 -> {
                return FactionEnum.FC_TENNO;
            }
            default -> {
                return FactionEnum.FC_NONE;
            }
        }
    }

    @Transient
    @JsonIgnore
    public MissionTypeEnum getMissionType() {
        switch (nodeType) {
            case 0 -> {
                return MissionTypeEnum.MT_ASSASSINATION;
            }
            case 1 -> {
                return MissionTypeEnum.MT_EXTERMINATION;
            }
            case 2 -> {
                return MissionTypeEnum.MT_SURVIVAL;
            }
            case 3 -> {
                return MissionTypeEnum.MT_RESCUE;
            }
            case 4 -> {
                return MissionTypeEnum.MT_SABOTAGE;
            }
            case 5 -> {
                return MissionTypeEnum.MT_CAPTURE;
            }
            case 7 -> {
                return MissionTypeEnum.MT_INTEL;
            }
            case 8 -> {
                return MissionTypeEnum.MT_DEFENSE;
            }
            case 9 -> {
                return MissionTypeEnum.MT_MOBILE_DEFENSE;
            }
            case 13 -> {
                return MissionTypeEnum.MT_TERRITORY;
            }
            case 14 -> {
                return MissionTypeEnum.MT_RETRIEVAL;
            }
            case 15 -> {
                return MissionTypeEnum.MT_HIVE;
            }
            case 17 -> {
                return MissionTypeEnum.MT_EXCAVATE;
            }
            case 21 -> {
                return MissionTypeEnum.MT_SALVAGE;
            }
            case 22 -> {
                return MissionTypeEnum.MT_ARENA;
            }
            case 24, 25 -> {
                return MissionTypeEnum.MT_PURSUIT;
            }
            case 26 -> {
                return MissionTypeEnum.MT_ASSAULT;
            }
            case 27 -> {
                return MissionTypeEnum.MT_EVACUATION;
            }
            case 28, 31 -> {
                return MissionTypeEnum.MT_LANDSCAPE;
            }
            case 33 -> {
                return MissionTypeEnum.MT_ARTIFACT;
            }
            case 34 -> {
                return MissionTypeEnum.MT_VOID_FLOOD;
            }
            case 35 -> {
                return MissionTypeEnum.MT_VOID_CASCADE;
            }
            case 36 -> {
                return MissionTypeEnum.MT_VOID_ARMAGEDDON;
            }
            case 38 -> {
                return MissionTypeEnum.MT_ALCHEMY;
            }
            case 40 -> {
                return MissionTypeEnum.MT_LEGACYTE_HARVEST;
            }
            case 41 -> {
                return MissionTypeEnum.MT_SHRINE_DEFENSE;
            }
            case 42 -> {
                return MissionTypeEnum.MT_FACEOFF;
            }
            default -> {
                return MissionTypeEnum.MT_DEFAULT;
            }
        }
    }
}
