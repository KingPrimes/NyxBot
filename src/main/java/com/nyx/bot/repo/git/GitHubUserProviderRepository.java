package com.nyx.bot.repo.git;


import com.nyx.bot.entity.git.GitHubUserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubUserProviderRepository extends JpaRepository<GitHubUserProvider, Long>, JpaSpecificationExecutor<GitHubUserProvider> {
}
