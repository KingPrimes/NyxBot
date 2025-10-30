package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.NotTranslation;
import com.nyx.bot.modules.warframe.entity.Translation;
import com.nyx.bot.modules.warframe.repo.NotTranslationRepository;
import com.nyx.bot.modules.warframe.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 未翻译数据
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.not_translation", description = "未翻译中英文数据")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/notTranslation")
public class NotTranslationController extends BaseController {

    @Resource
    NotTranslationRepository notTranslationRepository;
    @Resource
    TranslationService translationService;

    @Operation(
            summary = "获取未翻译数据详情",
            description = "根据ID获取未翻译数据的详细信息",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "未翻译数据ID",
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
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @GetMapping("/add/{id}")
    public AjaxResult add(@PathVariable Long id) {
        AjaxResult ar = success();
        ar.put("id", id);
        notTranslationRepository.findById(id).ifPresent(n -> ar.put("key", n.getNotTranslation()));
        return ar;
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @Operation(
            summary = "获取未翻译数据列表",
            description = "分页获取未翻译数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "未翻译数据查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NotTranslation.class)
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
    public TableDataInfo list(@RequestBody NotTranslation t) {
        ExampleMatcher notTranslation = ExampleMatcher.matching().withMatcher("notTranslation", ExampleMatcher.GenericPropertyMatcher::contains)
                .withIgnoreCase();
        Example<NotTranslation> notTranslationExample = Example.of(t, notTranslation);

        return getDataTable(notTranslationRepository.findAll(notTranslationExample, PageRequest.of(t.getCurrent() - 1, t.getSize())));
    }

    /**
     * 添加词典
     *
     * @param t 词典内容
     */
    @Operation(
            summary = "保存翻译数据",
            description = "将未翻译数据保存为翻译数据",
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
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody Translation t) {
        notTranslationRepository.deleteById(t.getId());
        Translation translation = new Translation();
        translation.setCn(t.getCn());
        translation.setEn(t.getEn());
        translation.setIsSet(t.getIsSet());
        translation.setIsPrime(t.getIsPrime());
        return toAjax(translationService.save(translation) != null);
    }


}
