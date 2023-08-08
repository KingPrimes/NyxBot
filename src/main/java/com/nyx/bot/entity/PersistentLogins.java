package com.nyx.bot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


import java.util.Date;

/**
 * 记住我实体类
 */
@Data
@Entity
@Table(name = "persistent_logins")
public class PersistentLogins {

    @Id
    String series;

    String username;

    String token;

    Date lastUsed;

}
