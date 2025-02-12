package com.nyx.bot.entity.bot.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"prove"}))
@JsonView(Views.View.class)
public class ProveBlack extends BaseEntity {
    @Id
    @GeneratedValue
    Long id;

    @NotEmpty(message = "bot.not.empty")
    Long botUid;
    @NotEmpty(message = "prove.not.empty")
    Long proveUid;
}
