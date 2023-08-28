package com.nyx.bot.entity;

import com.nyx.bot.enums.ServicesEnums;
import jakarta.persistence.*;
import lombok.Data;

/**
 * 服务
 */
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"service"}))
@Entity
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    ServicesEnums service;
    Boolean swit;
}
