package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.aop.Validated;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 订阅
 */
@Getter
@Setter
@Entity
@Table
public class MissionSubscribe extends BaseEntity {

    String groupName;
    //发送消息的Bot
    Long subBotUid;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotEmpty(message = "id.not.empty",groups = Validated.class)
    private Long id;

    @Column(unique = true)
    private Long subGroup;

    @OneToMany(mappedBy = "missionSubscribe",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    private Set<MissionSubscribeUser> users = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MissionSubscribe that = (MissionSubscribe) o;
        return Objects.equals(groupName, that.groupName) && Objects.equals(subBotUid, that.subBotUid) && Objects.equals(id, that.id) && Objects.equals(subGroup, that.subGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), groupName, subBotUid, id, subGroup);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("groupName", groupName)
                .append("subBotUid", subBotUid)
                .append("id", id)
                .append("subGroup", subGroup)
                .append("users", users)
                .toString();
    }
}
