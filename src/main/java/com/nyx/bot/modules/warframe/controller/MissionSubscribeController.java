package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Warframe 事件订阅组
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "data.warframe.subscribe", description = "Warframe 事件订阅组接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/data/warframe/subscribe")
@Validated
public class MissionSubscribeController extends BaseController {


    private final SubscriptionApplicationService subscriptionService;

    public MissionSubscribeController(SubscriptionApplicationService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(
            summary = "获取订阅类型列表",
            description = "获取所有可用的订阅类型",
            method = HttpMethod.GET,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "code":200,
                                                                "msg":"成功",
                                                                "data":[
                                                                    {
                                                                        "ERROR":"没有此数值！",
                                                                        "ALERTS":"警报"
                                                                    }
                                                                ]
                                                            }
                                                            """
                                            )
                                    }
                            )}
                    )
            }
    )
    @GetMapping("/sub")
    public ApiResponse<Object> subscribe() {
        return success(Arrays.stream(SubscribeEnums.values()).collect(Collectors.toMap(SubscribeEnums::name, SubscribeEnums::getNAME)));
    }

    @Operation(
            summary = "获取任务类型枚举",
            description = "获取所有Warframe任务类型枚举值",
            method = HttpMethod.GET,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "code":200,
                                                                "msg":"成功",
                                                                "data":[
                                                                    {
                                                                        "ERROR":"没有此数值！",
                                                                        "Assassination":"刺杀"
                                                                    }
                                                                ]
                                                            }
                                                            """
                                            )
                                    }
                            )}
                    )
            }
    )
    @GetMapping("/type")
    public ApiResponse<Object> getTypeEnums() {
        return success(Arrays.stream(MissionTypeEnum.values()).collect(Collectors.toMap(MissionTypeEnum::name, MissionTypeEnum::getName)));
    }

    @Operation(
            summary = "获取订阅组列表",
            description = "分页获取订阅组数据列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "订阅组查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MissionSubscribe.class)
                    )}
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageData.class)
                            )}
                    )
            }
    )
    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<@NonNull MissionSubscribe> page = subscriptionService.findAllSubscriptions(ms.getSubGroup(), pageable);
        return getDataTable(page);
    }

    @Operation(
            summary = "获取订阅用户列表",
            description = "根据订阅组ID分页获取订阅用户列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "订阅用户查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MissionSubscribe.class)
                    )}
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageData.class)
                            )}
                    )
            }
    )
    @PostMapping("/user/list")
    public ApiResponse<PageData<?>> userList(@RequestBody @Validated MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<@NonNull MissionSubscribeUser> page = subscriptionService.findAllUsersBySubId(ms.getId(), pageable);
        return getDataTable(page);
    }

    @Operation(
            summary = "获取检查类型列表",
            description = "根据订阅用户ID分页获取检查类型列表",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "检查类型查询参数",
                    required = true,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MissionSubscribeUser.class)
                    )}
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "查询成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageData.class)
                            )}
                    )
            }
    )
    @PostMapping("/type/list")
    public ApiResponse<PageData<?>> typeList(@RequestBody @Validated MissionSubscribeUser user) {
        Pageable pageable = PageRequest.of(user.getCurrent() - 1, user.getSize());
        Page<@NonNull MissionSubscribeUserCheckType> page = subscriptionService.findAllCheckTypesByUserId(user.getId(), pageable);
        return getDataTable(page);
    }

    // 删除订阅组
    @Operation(
            summary = "删除订阅组",
            description = "根据ID删除指定的订阅组",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "订阅组ID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subscriptionService.deleteSubscribeGroup(id);
        return success();
    }

    @Operation(
            summary = "删除订阅用户",
            description = "根据ID删除指定的订阅用户",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "订阅用户ID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/user/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        subscriptionService.deleteSubscribeUser(id);
        return success();
    }

    @Operation(
            summary = "删除检查类型",
            description = "根据ID删除指定的检查类型",
            method = HttpMethod.DELETE,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "检查类型ID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/type/{id}")
    public ApiResponse<Void> deleteCheckType(@PathVariable Long id) {
        subscriptionService.deleteCheckType(id);
        return success();
    }


}
