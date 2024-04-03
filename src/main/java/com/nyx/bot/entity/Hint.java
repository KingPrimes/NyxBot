package com.nyx.bot.entity;


import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Table
@Data
@Entity
public class Hint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "longtext")
    String hint;
}
