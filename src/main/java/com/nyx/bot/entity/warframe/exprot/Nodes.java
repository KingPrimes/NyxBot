package com.nyx.bot.entity.warframe.exprot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.res.enums.FactionEnum;
import com.nyx.bot.res.enums.MissionTypeEnum;
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

    @JsonProperty("name")
    String name;

    @JsonProperty("systemName")
    String systemName;

    @JsonProperty("systemIndex")
    Integer systemIndex;

    @JsonProperty("nodeType")
    Integer nodeType;

    @JsonProperty("productCategory")
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
    public String getFactionName() {
        switch (factionIndex) {
            case 0 -> {
                return FactionEnum.FC_GRINEER.getName();
            }
            case 1 -> {
                return FactionEnum.FC_CORPUS.getName();
            }
            case 2 -> {
                return FactionEnum.FC_INFESTATION.getName();
            }
            case 3 -> {
                return FactionEnum.FC_OROKIN.getName();
            }
            case 4 -> {
                return FactionEnum.FC_CORRUPTED.getName();
            }
            case 5 -> {
                return FactionEnum.FC_SENTIENT.getName();
            }
            case 6 -> {
                return FactionEnum.FC_NARMER.getName();
            }
            case 7 -> {
                return FactionEnum.FC_MURMUR.getName();
            }
            case 8 -> {
                return FactionEnum.FC_SCALDRA.getName();
            }
            case 9 -> {
                return FactionEnum.FC_TECHROT.getName();
            }
            case 10 -> {
                return FactionEnum.FC_DUVIRI.getName();
            }
            case 11 -> {
                return FactionEnum.FC_MITW.getName();
            }
            default -> {
                return "未知派系";
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
