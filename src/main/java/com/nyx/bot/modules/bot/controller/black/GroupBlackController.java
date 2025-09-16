package com.nyx.bot.modules.bot.controller.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.entity.black.GroupBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/black/group")
public class GroupBlackController extends BaseController {

    @Resource
    BlackService bs;

    @ApiOperation("查询群聊黑名单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gb", value = "群聊黑名单对象", dataType = "GroupBlack", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "groupUid", value = "123456"),
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
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
                                        "botUid": 123456,
                                        "groupUid": 123456,
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
    public TableDataInfo list(@RequestBody GroupBlack gb) {
        return getDataTable(bs.list(gb));
    }

    @ApiOperation("新增群聊黑名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gb", value = "群聊黑名单对象", dataType = "GroupBlack", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "groupUid", value = "123456"),
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "新增成功"),
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody GroupBlack gb) {
        return toAjax(bs.save(gb));
    }

    @ApiOperation("删除群聊黑名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "群聊黑名单id", dataType = "Long", paramType = "path")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "删除成功"),
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.remove(id));
    }

}
