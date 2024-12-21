package com.nyx.bot.entity.bot.black;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"group"}))
public class GroupBlack extends BaseEntity {
    @Id
    @GeneratedValue
    Long id;

    Long bot;

    Long groupUid;
}
