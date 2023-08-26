package com.nyx.bot.entity.sys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * Token
 */
@Data
@Entity
@Table
public class PersistentLogins {
    @Id
    @Column(length = 64, nullable = false)
    String series;
    @Column(length = 64, nullable = false)
    String username;
    @Column(length = 64, nullable = false)
    String token;
    @Column(length = 64, nullable = false)
    Date lastUsed;
}
