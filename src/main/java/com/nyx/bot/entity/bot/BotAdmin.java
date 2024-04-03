package com.nyx.bot.entity.bot;

import com.nyx.bot.core.dao.BaseEntity;
import com.nyx.bot.enums.PermissionsEnums;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "adminUid", columnNames = {"botUid", "adminUid", "permissions"}))
@Data
public class BotAdmin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long botUid;
    Long adminUid;
    PermissionsEnums permissions;

}
