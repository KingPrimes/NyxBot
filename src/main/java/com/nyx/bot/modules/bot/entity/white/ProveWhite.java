package com.nyx.bot.modules.bot.entity.white;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.dao.BaseEntity;
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

    @NotEmpty(message = "bot.not.empty")
    Long botUid;

    @NotEmpty(message = "prove.not.empty")
    Long proveUid;
}
