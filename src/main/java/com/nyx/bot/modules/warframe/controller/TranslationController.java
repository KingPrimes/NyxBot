package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.Translation;
import com.nyx.bot.modules.warframe.repo.TranslationRepository;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Warframe 翻译数据
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)

@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/translation")
public class TranslationController extends BaseController {

    @Resource
    TranslationRepository repository;

    @Operation(
            summary = "获取翻译数据详情",
            description = "根据ID获取翻译数据的详细信息",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "翻译数据ID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Translation.class)
                            )}
                    )
            }
    )
    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = success();
        repository.findById(id).ifPresent(t -> ar.put("translation", t));
        return ar;
    }

    @Operation(
            summary = "保存翻译数据",
            description = "保存翻译数据",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "翻译数据内容",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Translation.class)
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "保存成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Translation.class)
                            )}
                    )
            }
    )
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody Translation t) {
        repository.save(t);
        return success();
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @Operation(
            summary = "获取翻译数据列表",
            description = "分页获取翻译数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "翻译数据查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Translation.class),
                            examples = @ExampleObject(value = """
                                    {"cn":"中文", "is_prime":true, "is_set":true}
                                    """)
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Translation.class)
                            )}
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody Translation t) {
        return getDataTable(repository.findAllPageable(
                t.getCn(),
                t.getIsPrime(),
                t.getIsSet(),
                PageRequest.of(t.getCurrent() - 1, t.getSize())
        ));
    }

    /**
     * 更新词典
     */
    @Operation(
            summary = "更新翻译数据",
            description = "更新翻译相关数据",
            method = HttpMethod.POST,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "更新任务已启动",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/update")
    public AjaxResult update() {
//        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
//            if (flag) {
//                CompletableFuture.runAsync(WarframeDataSource::initTranslation);
//            }
//        });
        return success(I18nUtils.RequestTaskRun());
    }

    @Operation(
            summary = "推送翻译数据",
            description = "将翻译数据推送到Git仓库",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "提交信息",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = """
                                    {"commit":"提交信息"}
                                    """)
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "推送成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String, String> commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            build.pull();
            List<Translation> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/translation.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit.get("commit"), branchName, "warframe/translation.json");
            return toAjax(true);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }


}
