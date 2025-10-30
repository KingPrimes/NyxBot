package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.entity.git.GitHubUserProvider;
import com.nyx.bot.repo.git.GitHubUserProviderRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GitHub数据仓库配置
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.github", description = "GitHub数据仓库配置")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/git")
public class GitHubUserProviderController extends BaseController {

    @Resource
    GitHubUserProviderRepository gitRepository;

    @Operation(
            summary = "获取GitHub用户配置",
            description = "获取GitHub用户配置",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功",
                            content = {
                                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {
                                                    @ExampleObject(value = """
                                                            {
                                                                "code": 200,
                                                                "msg": "获取成功",
                                                                "data": {
                                                                    "gitUrl": "https://github.com/nyxbot/nyxbot.git",
                                                                    "gitUsername": "nyxbot",
                                                                    "gitPassword": "123456"
                                                                }
                                                            }
                                                            """
                                                    )
                                            }
                                    )
                            }
                    )
            }
    )
    @GetMapping
    public AjaxResult html() {
        AjaxResult ar = success();
        List<GitHubUserProvider> all = gitRepository.findAll();
        GitHubUserProvider provider = new GitHubUserProvider();
        if (!all.isEmpty()) {
            provider = all.getFirst();
        }
        provider.setGitUrl(JgitUtil.getOriginUrl(JgitUtil.lockPath));
        ar.put("data", provider);
        return ar;
    }

    @Operation(
            summary = "保存GitHub用户配置",
            description = "保存GitHub用户配置",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GitHub用户配置",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = GitHubUserProvider.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "gitUrl": "https://github.com/nyxbot/nyxbot.git",
                                                        "gitUsername": "nyxbot",
                                                        "gitPassword": "123456"
                                                    }
                                                    """)
                                    }
                            )
                    }
            )
    )
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
