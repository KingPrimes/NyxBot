package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.service.RelicsService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 遗物
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.relics", description = "Warframe 遗物")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/relics")
public class RelicsController extends BaseController {

    RelicsService rs;

    public RelicsController(RelicsService rs) {
        this.rs = rs;
    }

    @Operation(
            summary = "获取遗物列表",
            description = "分页获取遗物数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "遗物查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Relics.class),
                            examples = @ExampleObject(value = """
                                    {"name":"A1"}
                                    """)
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
    public TableDataInfo list(@RequestBody Relics relics) {
        return rs.findAllPageable(relics);
    }


}
