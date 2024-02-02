package com.nyx.bot.controller.config;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/config/git")
public class GitHubUserProviderController extends BaseController {

    String prefix = "config/git/";

    @Resource
    GitHubUserProviderRepository gitRepository;

    @GetMapping("/html")
    public String html(ModelMap mapp) {
        List<GitHubUserProvider> all = gitRepository.findAll();
        if (!all.isEmpty()) {
            mapp.put("git", all.get(0));
        }
        return prefix + "github";
    }


    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(GitHubUserProvider gitHubUserProvider) {
        gitRepository.save(gitHubUserProvider);
        return success();
    }


}
