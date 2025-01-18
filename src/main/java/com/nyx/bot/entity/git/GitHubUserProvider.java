package com.nyx.bot.entity.git;

import com.nyx.bot.annotation.InternationalizedNotEmpty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "github", columnNames = {"userName"}))
@Entity
public class GitHubUserProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @InternationalizedNotEmpty(message = "git.name.not.empty")
    @Column(nullable = false)
    String userName;
    @InternationalizedNotEmpty(message = "git.token.not.empty")
    @Column(nullable = false)
    String passWord;
    @InternationalizedNotEmpty(message = "git.url.not.empty")
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
