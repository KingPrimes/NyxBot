package com.nyx.bot.entity.bot.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
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

    @NotEmpty(message = "bot.not.empty")
    Long botUid;
    @NotEmpty(message = "group.not.empty")
    Long groupUid;
}
