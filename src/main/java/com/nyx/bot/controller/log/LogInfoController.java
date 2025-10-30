package com.nyx.bot.controller.log;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.LogTitleEnum;
import com.nyx.bot.modules.system.entity.LogInfo;
import com.nyx.bot.modules.system.repo.LogInfoRepository;
import com.nyx.bot.utils.StringUtils;
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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "log_info")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    @Resource
    LogInfoRepository repository;


    @Operation(summary = "获取代码列表",
            description = "获取系统支持的所有代码列表",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class)
                                    )
                            }
                    )
            }
    )
    @GetMapping("/codes")
    public AjaxResult info() {
        return success().put("data", Arrays.stream(Codes.values())
                .map(c -> Map.of("label", StringUtils.removeMatcher(c.getComm()), "value", StringUtils.removeMatcher(c.getComm())))
                .collect(Collectors.toList())
        );
    }

    @Operation(summary = "获取日志标题列表",
            description = "获取系统支持的所有日志标题列表",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class)
                                    )
                            }
                    )
            }
    )
    @GetMapping("/titles")
    public AjaxResult logTitle() {
        return success().put("data", Arrays.stream(LogTitleEnum.values())
                .map(t -> Map.of("label", t.getTitle(), "value", t.name()))
                .toList());
    }

    // 分页条件查询
    @Operation(summary = "日志列表",
            description = "分页获取日志信息列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "日志查询条件",
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = LogInfo.class)
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
                                            schema = @Schema(implementation = TableDataInfo.class)
                                    )
                            }
                    )
            }
    )
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody LogInfo info) {
        return getDataTable(repository.findAllPageable(
                info.getTitle(),
                info.getCode(),
                info.getGroupUid(),
                PageRequest.of(info.getCurrent() - 1, info.getSize())));
    }

    @Operation(summary = "日志详情",
            description = "根据日志ID获取日志详细信息",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "logId",
                            description = "日志ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class)
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
                                            examples = {
                                                    @ExampleObject(
                                                            value = """
                                                                    {
                                                                        "code":200,
                                                                        "mag":"",
                                                                        "data":{
                                                                            "id":0,
                                                                            "title":"PLUGIN"，
                                                                            "code":""
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
    @GetMapping("/detail/{logId}")
    public AjaxResult detail(@PathVariable("logId") Long logId) {
        AjaxResult ar = success();
        repository.findById(logId).ifPresent(l -> ar.put("data", l));
        return ar;
    }
}
