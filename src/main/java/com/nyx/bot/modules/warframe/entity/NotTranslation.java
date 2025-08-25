package com.nyx.bot.modules.warframe.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false, exclude = {"notTranslation"})
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"notTranslation"}))
@NoArgsConstructor
@JsonView(Views.View.class)
public class NotTranslation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String notTranslation;

    public NotTranslation(String notTranslation) {
        this.notTranslation = notTranslation;
    }
}
