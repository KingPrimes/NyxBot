package com.nyx.bot.modules.bot.controller.admin;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config/bot/admin")
public class BotAdminController extends BaseController {

    @Resource
    BotAdminRepository botAdminRepository;


    @ApiOperation("获取机器人管理员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "botUid", value = "机器人UID", required = true, dataType = "BotAdmin", paramType = "query", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456")
            })),
            @ApiImplicitParam(name = "current", value = "当前页", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Long", paramType = "query"),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", response = TableDataInfo.class, examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            {
                                "total": 1,
                                "size": 10,
                                "current": 1,
                                "records": [
                                    {
                                        "id": 1,
                                        "botUid": "1",
                                        "adminUid": "1",
                                        "permissions": "SUPER_ADMIN"
                                    }
                                ]
                            }
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
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

    @ApiOperation("获取机器人管理员权限列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功",examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            [
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
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/permissions")
    public AjaxResult getPermissions() {
        return success().put("data", getPe());
    }

    @ApiOperation("保存机器人管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ba", value = "BotAdmin", required = true, dataType = "BotAdmin", paramType = "form", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "adminUid", value = "123456"),
                    @ExampleProperty(mediaType = "permissions", value = "SUPER_ADMIN")
            })),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "保存成功")
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
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

    @ApiOperation("删除机器人管理员")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "BotAdmin ID", required = true, dataType = "Long", paramType = "path", examples = @Example(value = {
                    @ExampleProperty(mediaType = "id", value = "123456")
            })),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "删除成功")
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        botAdminRepository.deleteById(id);
        return success();
    }
}
