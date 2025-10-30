package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.modules.warframe.entity.RivenTrend;
import com.nyx.bot.modules.warframe.repo.RivenTrendRepository;
import com.nyx.bot.modules.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Warframe 紫卡倾向
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.rivenTrend", description = "Warframe紫卡倾向数据接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/rivenTrend")
public class RivenTrendController extends BaseController {

    @Resource
    RivenTrendRepository repository;

    @Operation(
            summary = "获取紫卡倾向添加页面数据",
            description = "获取紫卡倾向添加页面所需的初始化数据",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @GetMapping("/add")
    public AjaxResult add() {
        return success().put("types", RivenTrendTypeEnum.values());
    }

    @Operation(
            summary = "获取紫卡倾向编辑页面数据",
            description = "根据ID获取紫卡倾向的详细信息用于编辑",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "紫卡倾向ID",
                            required = true,
                            schema = @Schema(implementation = Long.class)
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = success().put("types", RivenTrendTypeEnum.values());
        if (ar != null) {
            ar.put("translation", repository.findById(id));
        }
        return ar;
    }

    @Operation(
            summary = "保存紫卡倾向数据",
            description = "保存紫卡倾向数据",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "紫卡倾向数据",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RivenTrend.class)
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "保存成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody RivenTrend t) {
        t.setOldDot(RivenTrendEnum.getRivenTrendDot(t.getOldNum()));
        t.setNewDot(RivenTrendEnum.getRivenTrendDot(t.getNewNum()));
        return toAjax(Math.toIntExact(repository.save(t).getId()));
    }

    @Operation(
            summary = "获取紫卡倾向列表",
            description = "分页获取紫卡倾向数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "紫卡倾向查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RivenTrend.class)
                    )}
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TableDataInfo.class)
                            )}
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody RivenTrend rt) {
        return getDataTable(repository.findAllPageable(rt.getTrendName(),
                PageRequest.of(
                        rt.getCurrent() - 1,
                        rt.getSize()
                )
        ));
    }

    @Operation(
            summary = "初始化紫卡倾向数据",
            description = "初始化紫卡倾向相关数据",
            method = HttpMethod.POST,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "初始化任务已启动",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/init")
    public AjaxResult init() {
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
            if (flag) {
                CompletableFuture.runAsync(WarframeDataSource::getRivenTrend);
            }
        });
        return success(I18nUtils.RequestTaskRun());
    }

    @Operation(
            summary = "更新紫卡倾向数据",
            description = "异步更新紫卡倾向数据",
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
        AsyncUtils.me().execute(() -> new RivenDispositionUpdates().upRivenTrend(), AsyncBeanName.InitData);
        return success(I18nUtils.RequestTaskRun());
    }

    @Operation(
            summary = "推送紫卡倾向数据",
            description = "将紫卡倾向数据推送到Git仓库",
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
            List<RivenTrend> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/riven_trend.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit.get("commit"), branchName, "warframe/riven_trend.json");
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
