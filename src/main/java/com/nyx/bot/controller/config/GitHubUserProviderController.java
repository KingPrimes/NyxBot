package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import io.swagger.annotations.*;
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
    @ApiOperation("获取GitHub用户配置")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = AjaxResult.class,examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            {
                                "gitUrl": "https://github.com/nyxbot/nyxbot.git",
                                "gitUsername": "nyxbot",
                                "gitPassword": "123456"
                            }
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
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
    @ApiOperation("保存GitHub用户配置")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = AjaxResult.class,examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "保存成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            {
                                "gitUrl": "https://github.com/nyxbot/nyxbot.git",
                                "gitUsername": "nyxbot",
                                "gitPassword": "123456"
                            }
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public AjaxResult save(@Validated @RequestBody GitHubUserProvider gitHubUserProvider) {
        if (!gitHubUserProvider.isValidGitUrl()) {
            return AjaxResult.error(I18nUtils.RequestValidGitUrl());
        }
        gitRepository.save(gitHubUserProvider);
        JgitUtil.restOriginUrl(gitHubUserProvider.getGitUrl(), JgitUtil.lockPath);
        return success();
    }


}
