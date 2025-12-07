package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.service.BotsService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bot
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "Bearer ",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER
)
@Tag(name = "config.bot", description = "机器人管理接口")
@SecurityRequirement(name = "Bearer")
@RestController
@RequestMapping("/config/bot")
public class BotController extends BaseController {

    private final BotsService botsService;

    public BotController(BotsService botsService) {
        this.botsService = botsService;
    }

    @Operation(
            summary = "获取机器人列表",
            description = "获取机器人列表",
            method = HttpMethod.GET,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = TableDataInfo.class),
                                            examples = {
                                                    @ExampleObject(
                                                            value = """
                                                                    {
                                                                        "code": 200,
                                                                        "msg": "获取成功",
                                                                        "data": [
                                                                            {
                                                                                "value": 123456,
                                                                                "label": "机器人1"
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
    @GetMapping("/bots")
    public AjaxResult getBots() {
        return botsService.getBots();
    }

    @Operation(
            summary = "获取好友列表",
            description = "获取好友列表",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "botUid",
                            description = "机器人UID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            example = "123456"
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
                                                                        "code": 200,
                                                                        "msg": "获取成功",
                                                                        "data": [
                                                                            {
                                                                                "value": 123456,
                                                                                "label": "好友1"
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
    @GetMapping("/friend/{botUid}")
    public AjaxResult getFriendList(@PathVariable Long botUid) {
        return botsService.getFriendList(botUid);
    }

    @Operation(
            summary = "获取群列表",
            description = "获取群列表",
            method = HttpMethod.GET,
            parameters = {
                    @Parameter(
                            name = "botUid",
                            description = "机器人UID",
                            required = true,
                            schema = @Schema(implementation = Long.class),
                            example = "123456"
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
                                                                        "code": 200,
                                                                        "msg": "获取成功",
                                                                        "data": [
                                                                            {
                                                                                "value": 123456,
                                                                                "label": "群1"
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
    @GetMapping("/group/{botUid}")
    public AjaxResult getGroupList(@PathVariable Long botUid) {
        return botsService.getGroupList(botUid);
    }


}
