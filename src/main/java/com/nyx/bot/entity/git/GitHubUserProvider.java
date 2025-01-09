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
    @NotEmpty(message = "Git账户不能为空")
    @Column(nullable = false)
    String userName;
    @NotEmpty(message = "GitToken不能为空")
    @Column(nullable = false)
    String passWord;
    @NotEmpty(message = "仓库地址不能为空")
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
