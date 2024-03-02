package com.nyx.bot.entity.bot.white;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"prove"}))
public class ProveWhite extends BaseEntity {
    @Id
    @GeneratedValue
    Long id;

    Long prove;
}
