package com.nyx.bot.modules.warframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.Ephemeras;
import com.nyx.bot.modules.warframe.repo.EphemerasRepository;
import com.nyx.bot.utils.I18nUtils;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Warframe 幻纹
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.ephemeras", description = "Warframe 幻纹")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    EphemerasRepository ephemerasRepository;

    public EphemerasController(EphemerasRepository ephemerasRepository) {
        this.ephemerasRepository = ephemerasRepository;
    }

    @Operation(
            summary = "查询幻纹列表",
            description = "根据条件查询幻纹列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "幻纹对象",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Ephemeras.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "name": "幻纹",
                                                        "current": 1,
                                                        "size": 10
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
                                                                    "id": "xxx",
                                                                    "name": "幻纹",
                                                                    "element": "火焰",
                                                                    "icon": "图标地址"
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
    public TableDataInfo list(@RequestBody Ephemeras e) {
        return getDataTable(ephemerasRepository.findAllPageable(
                e.getName(),
                PageRequest.of(
                        e.getCurrent() - 1,
                        e.getSize())
        ));
    }

    @Operation(
            summary = "更新幻纹数据",
            description = "从数据源更新幻纹数据",
            method = HttpMethod.POST,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = Ephemeras.class),
                                            examples = {@ExampleObject(value = """
                                                    {
                                                        "code": 200,
                                                        "msg": "操作成功"
                                                    }
                                                    """)
                                            }
                                    )
                            }
                    )
            }
    )
    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getEphemeras);
        return success(I18nUtils.RequestTaskRun());
    }
}
