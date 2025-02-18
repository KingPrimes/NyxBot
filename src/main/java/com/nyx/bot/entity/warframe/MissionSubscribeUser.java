package com.nyx.bot.entity.warframe;

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


@Getter
@Setter
@Entity
@Table
public class MissionSubscribeUser {

    Long userId;
    String userName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_id")
    private MissionSubscribe missionSubscribe;

    @OneToMany(mappedBy = "subscribeUser",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<MissionSubscribeUserCheckType> checkTypes = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionSubscribeUser that = (MissionSubscribeUser) o;
        return Objects.equals(userId, that.userId) && Objects.equals(userName, that.userName) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("userId", userId)
                .append("userName", userName)
                .append("missionSubscribe", missionSubscribe)
                .toString();
    }
}
