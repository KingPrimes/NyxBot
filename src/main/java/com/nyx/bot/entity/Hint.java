package com.nyx.bot.entity;


import jakarta.persistence.*;
import lombok.Data;

@Table
@Data
@Entity
public class Hint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "longtext")
    String hint;
}
