package com.nyx.bot.entity.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.InternationalizedNotEmpty;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.PermissionsEnums;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "adminUid", columnNames = {"botUid", "adminUid", "permissions"}))
@Data
@JsonView(Views.View.class)
public class BotAdmin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @InternationalizedNotEmpty(message = "bot.not.empty")
    Long botUid;
    @InternationalizedNotEmpty(message = "admin.not.empty")
    Long adminUid;
    PermissionsEnums permissions;

    @JsonIgnore
    public boolean isValidatePermissions() {
        return permissions == PermissionsEnums.OTHER || permissions == PermissionsEnums.MANAGE;
    }
}
