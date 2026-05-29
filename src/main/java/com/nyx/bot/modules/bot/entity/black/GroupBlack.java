package com.nyx.bot.modules.bot.entity.black;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.dao.BaseEntity;
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
    @NotEmpty(message = "bot.not.empty")
    Long botUid;
    @NotEmpty(message = "group.not.empty")
    Long groupUid;

}
