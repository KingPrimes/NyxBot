package com.nyx.bot.controller.config;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
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
        GitHubUserProvider provider = new GitHubUserProvider();
        if (!all.isEmpty()) {
            provider = all.get(0);
        }
        provider.setGitUrl(JgitUtil.getOriginUrl(JgitUtil.lockPath));
        ar.put("data", provider);
        return ar;
    }


    @PostMapping
    public AjaxResult save(@Validated @RequestBody GitHubUserProvider gitHubUserProvider) {
        if (!gitHubUserProvider.isValidGitUrl()) {
            return AjaxResult.error(I18nUtils.RequestValidGitUrl());
        }
        gitRepository.save(gitHubUserProvider);
        JgitUtil.restOriginUrl(gitHubUserProvider.getGitUrl(), JgitUtil.lockPath);
        return success();
    }


}
