package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.utils.I18nUtils;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Warframe 别名
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.alias", description = "Warframe 别名管理接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/alias")

public class AliasController extends BaseController {

    @Resource
    AliasRepository repository;

    @Operation(
            summary = "查询别名列表",
            description = "根据条件查询别名列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "别名对象",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(name = "cn",
                                            implementation = String.class,
                                            example = "牛",
                                            defaultValue = "牛"
                                    ),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "cn": "牛"
                                                    }
                                                    """)
                                    }
                            )
                    }
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = TableDataInfo.class),
                                            examples = {@ExampleObject(value = """
                                                    {
                                                        "code": 200,
                                                        "data": {
                                                            "total": 1,
                                                            "size": 10,
                                                            "current": 1,
                                                            "records": [
                                                                {
                                                                    "id": 1,
                                                                    "cn": "战甲",
                                                                    "en": "warframe"
                                                                }
                                                            ]
                                                        }
                                                    }
                                                    """)
                                            }
                                    )
                            }
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody Alias alias) {
        return getDataTable(repository.findByLikeCn(alias.getCn(), PageRequest.of(alias.getCurrent() - 1, alias.getSize())));
    }

    @Operation(
            summary = "更新别名数据",
            description = "从数据源更新别名数据",
            method = HttpMethod.POST
    )
    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getAlias);
        return success(I18nUtils.RequestTaskRun());
    }

    @Operation(
            summary = "新增别名",
            description = "新增或保存别名信息",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "别名对象",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Alias.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "cn": "战甲",
                                                        "en": "warframe"
                                                    }
                                                    """)
                                    }
                            )
                    }
            )
    )
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody Alias a) {
        if (!a.isValidEnglish()) {
            return error(I18nUtils.message("request.valid.alias.en"));
        }
        if (!a.isValidChinese()) {
            return error(I18nUtils.message("request.valid.alias.ch"));
        }
        Optional<Alias> alias = repository.findByCnAndEn(a.getCn(), a.getEn());
        if (alias.isPresent()) {
            return error(I18nUtils.message("request.error.data.already.exists"));
        }
        repository.save(a);
        return success();
    }

    @Operation(
            summary = "编辑别名",
            description = "根据ID获取别名信息",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "别名ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class),
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {@ExampleObject(value = """
                                                    {
                                                        "code": 200,
                                                        "msg": "操作成功",
                                                        "data": {
                                                            "alias": {
                                                                "id": 1,
                                                                "cn": "战甲",
                                                                "en": "warframe"
                                                            }
                                                        }
                                                    }
                                                    """)
                                            }
                                    )
                            }
                    )
            }
    )
    @GetMapping("/edit/{id}")
    public AjaxResult edit(@NonNull @PathVariable Long id) {
        AjaxResult ar = success();
        repository.findById(id).ifPresent(a -> ar.put("alias", a));
        return ar;
    }

}
