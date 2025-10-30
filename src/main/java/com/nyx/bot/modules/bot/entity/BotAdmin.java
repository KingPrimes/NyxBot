package com.nyx.bot.modules.bot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.dao.BaseEntity;
import com.nyx.bot.enums.PermissionsEnums;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @NotEmpty(message = "bot.not.empty")
    @DecimalMin(value = "10000", message = "qq.uid.invalid") // 最小5位数字(10000)
    @DecimalMax(value = "9999999999999", message = "qq.uid.invalid") // 最大13位数字
    Long botUid;

    @DecimalMin(value = "10000", message = "qq.uid.invalid") // 最小5位数字(10000)
    @DecimalMax(value = "9999999999999", message = "qq.uid.invalid") // 最大13位数字
    @NotEmpty(message = "admin.not.empty")
    Long adminUid;

    PermissionsEnums permissions;

    @JsonIgnore
    public boolean isValidatePermissions() {
        return permissions == PermissionsEnums.OTHER || permissions == PermissionsEnums.MANAGE;
    }
}
