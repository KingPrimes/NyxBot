package com.nyx.bot.entity.bot.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"group"}))
@JsonView(Views.View.class)
public class GroupWhite extends BaseEntity {
    @Id
    @GeneratedValue
    Long id;

    @NotNull(message = "{bot.not.empty}")
    Long botUid;
    @NotNull(message = "{group.not.empty}")
    Long groupUid;
}
