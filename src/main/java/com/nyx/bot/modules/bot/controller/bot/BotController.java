package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.service.BotsService;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config/bot")
public class BotController extends BaseController {

    @Resource
    BotsService botsService;

    @ApiOperation("获取机器人列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            [
                                {
                                    "value": "123456",
                                    "label": "机器人1"
                                }
                            ]
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/bots")
    public AjaxResult getBots() {
        return botsService.getBots();
    }

    @ApiOperation("获取好友列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            [
                                {
                                    "value": "123456",
                                    "label": "好友1"
                                }
                            ]
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "此机器人未链接")
    })
    @GetMapping("/friend/{botUid}")
    public AjaxResult getFriendList(@PathVariable Long botUid) {
        return botsService.getFriendList(botUid);
    }

    @ApiOperation("获取群列表")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data", value = """
                            [
                                {
                                    "value": "123456",
                                    "label": "群1"
                                }
                            ]
                            """)
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "此机器人未链接")
    })
    @GetMapping("/group/{botUid}")
    public AjaxResult getGroupList(@PathVariable Long botUid) {
        return botsService.getGroupList(botUid);
    }


}
