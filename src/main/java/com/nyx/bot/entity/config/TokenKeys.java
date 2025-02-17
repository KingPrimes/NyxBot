package com.nyx.bot.entity.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "token_keys")
public class TokenKeys {

    @Id
    private Long id;

    private String tks;
}
