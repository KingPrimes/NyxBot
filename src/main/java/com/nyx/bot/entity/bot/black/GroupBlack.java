package com.nyx.bot.entity.bot.black;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class GroupBlack extends BaseEntity {
    @Id
    @GeneratedValue
    Long id;

    Long groupUid;
}
