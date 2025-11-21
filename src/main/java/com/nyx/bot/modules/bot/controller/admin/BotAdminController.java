package com.nyx.bot.modules.bot.controller.admin;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bot 管理员
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.bot.admin", description = "机器人管理员管理接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/bot/admin")
public class BotAdminController extends BaseController {

    @Resource
    BotAdminRepository botAdminRepository;


    @Operation(
            summary = "获取机器人管理员列表",
            description = "获取机器人管理员列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "机器人管理员查询参数",
                    required = true,
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BotAdmin.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "botUid": 123456,
                                                        "current": 1,
                                                        "size": 10
                                                    }
                                                    """)
                                    })
                    }),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = TableDataInfo.class),
                                            examples = {
                                                    @ExampleObject(value = """
                                                            {
                                                                "total": 1,
                                                                "size": 10,
                                                                "current": 1,
                                                                "records": [
                                                                    {
                                                                        "id": 1,
                                                                        "botUid": 123456,
                                                                        "adminUid": 123456,
                                                                        "permissions": "SUPER_ADMIN"
                                                                    }
                                                                ]
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
    public TableDataInfo list(@RequestBody BotAdmin ba) {
        return getDataTable(botAdminRepository.findAllByBotUid(ba.getBotUid(), PageRequest.of(ba.getCurrent() - 1, ba.getSize())));
    }

    private List<Map<String, String>> getPe() {
        return Arrays.stream(PermissionsEnums.values())
                .filter(enums -> enums != PermissionsEnums.MANAGE && enums != PermissionsEnums.OTHER)
                .map(e -> Map.of("label", e.getStr(), "value", e.name()))
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "获取机器人管理员权限列表",
            description = "获取机器人管理员权限列表",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = AjaxResult.class),
                                            examples = {
                                                    @ExampleObject(value = """
                                                                {
                                                                    "code": 200,
                                                                    "msg": "获取成功",
                                                                    "data":  [
                                                                        {
                                                                            "label": "超级管理员用户",
                                                                            "value": "SUPER_ADMIN"
                                                                        },
                                                                        {
                                                                            "label": "后台用户",
                                                                            "value": "MANAGE"
                                                                        },
                                                                        {
                                                                            "label": "其他用户",
                                                                            "value": "OTHER"
                                                                        }
                                                                    ]
                                                                }
                                                            """
                                                    )
                                            }
                                    )
                            }
                    )
            }
    )
    @GetMapping("/permissions")
    public AjaxResult getPermissions() {
        return success().put("data", getPe());
    }

    @Operation(
            summary = "保存机器人管理员",
            description = "保存机器人管理员",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "BotAdmin",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BotAdmin.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "botUid": 123456,
                                                        "adminUid": 123456,
                                                        "permissions": "SUPER_ADMIN"
                                                    }
                                                    """)
                                    }
                            )
                    }
            )
    )
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody BotAdmin ba) {
        if (ba.isValidatePermissions()) {
            return error(I18nUtils.PermissionsBan());
        }
        Optional<BotAdmin> byPermissions = botAdminRepository.findByPermissions(ba.getPermissions());
        if (byPermissions.isPresent()) {
            if (byPermissions.get().getPermissions().equals(PermissionsEnums.SUPER_ADMIN) && ba.getBotUid().equals(byPermissions.get().getBotUid())) {
                return error(I18nUtils.PermissionsOne());
            }
        }
        botAdminRepository.save(ba);
        return success();
    }

    @Operation(
            summary = "删除机器人管理员",
            description = "删除机器人管理员",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "BotAdmin ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(implementation = Long.class),
                            example = "123456"
                    )
            }
    )
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@NonNull @PathVariable("id") Long id) {
        botAdminRepository.deleteById(id);
        return success();
    }
}
