package com.nyx.bot.entity.git;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "github", columnNames = {"userName"}))
@Entity
public class GitHubUserProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String userName;
    String passWord;
    String gitUrl;
}
