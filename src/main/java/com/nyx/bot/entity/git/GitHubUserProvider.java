package com.nyx.bot.entity.git;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "github", columnNames = {"userName"}))
@Entity
public class GitHubUserProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotEmpty(message = "{git.name.not.empty}")
    @Column(nullable = false)
    String userName;
    @NotEmpty(message = "{git.token.not.empty}")
    @Column(nullable = false)
    String passWord;
    @NotEmpty(message = "{git.url.not.empty}")
    @Column(nullable = false)
    String gitUrl;

    public GitHubUserProvider() {

    }

    public boolean isValidGitUrl() {
        String regex = "^(https?|git)://[\\w.-]+(:\\d+)?(/([\\w/_.-]*(\\?\\S+)?)?)?$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(this.gitUrl).matches();
    }
}
