package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserRepository;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
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


    private final MissionSubscribeRepository repository;

    private final SubscriptionApplicationService subscriptionService;

    private final MissionSubscribeUserRepository msu;

    private final MissionSubscribeUserCheckTypeRepository msuct;

    public MissionSubscribeController(MissionSubscribeRepository repository, SubscriptionApplicationService subscriptionService, MissionSubscribeUserRepository msu, MissionSubscribeUserCheckTypeRepository msuct) {
        this.repository = repository;
        this.subscriptionService = subscriptionService;
        this.msu = msu;
        this.msuct = msuct;
    }

    @Operation(
            summary = "获取订阅类型列表",
            description = "获取所有可用的订阅类型",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
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
    public AjaxResult subscribe() {
        return success().put("data", Arrays.stream(SubscribeEnums.values()).collect(Collectors.toMap(SubscribeEnums::name, SubscribeEnums::getNAME)));
    }

    @Operation(
            summary = "获取任务类型枚举",
            description = "获取所有Warframe任务类型枚举值",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
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
    public AjaxResult getTypeEnums() {
        return success().put("data", Arrays.stream(MissionTypeEnum.values()).collect(Collectors.toMap(MissionTypeEnum::name, MissionTypeEnum::getName)));
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
    public TableDataInfo list(@RequestBody MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<MissionSubscribe> page = repository.findAllPageable(ms.getSubGroup(), pageable);
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
    @PostMapping("/user/list")
    public TableDataInfo userList(@RequestBody @Validated MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<MissionSubscribeUser> page = msu.findAllBySUB_ID(ms.getId(), pageable);
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
    @PostMapping("/type/list")
    public TableDataInfo typeList(@RequestBody @Validated MissionSubscribeUser user) {
        Pageable pageable = PageRequest.of(user.getCurrent() - 1, user.getSize());
        Page<MissionSubscribeUserCheckType> page = msuct.findAllBySUBU_ID(user.getId(), pageable);
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
                    @ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
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
                    @ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/user/{id}")
    public AjaxResult deleteUser(@PathVariable Long id) {
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
                    @ApiResponse(
                            responseCode = "200",
                            description = "删除成功",
                            content = {@Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AjaxResult.class)
                            )}
                    )
            }
    )
    @DeleteMapping("/type/{id}")
    public AjaxResult deleteCheckType(@PathVariable Long id) {
        subscriptionService.deleteCheckType(id);
        return success();
    }


}
