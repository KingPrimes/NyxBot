package com.nyx.bot.controller.config;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config/git")
public class GitHubUserProviderController extends BaseController {

    @Resource
    GitHubUserProviderRepository gitRepository;

    @GetMapping
    public AjaxResult html() {
        AjaxResult ar = success();
        List<GitHubUserProvider> all = gitRepository.findAll();
        if (!all.isEmpty()) {
            ar.put("git", all.get(0));
        } else {
            ar.put("git", new GitHubUserProvider());
        }

        ar.put("gitUrl", JgitUtil.getOriginUrl(JgitUtil.lockPath));

        return ar;
    }


    @PostMapping("/save")
    public AjaxResult save(@RequestBody GitHubUserProvider gitHubUserProvider) {
        gitRepository.save(gitHubUserProvider);
        JgitUtil.restOriginUrl(gitHubUserProvider.getGitUrl(), JgitUtil.lockPath);
        return success();
    }


}
