package com.nyx.bot.modules.bot.controller.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.bot.entity.white.GroupWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/group")
public class GroupWhiteController extends BaseController {

    @Resource
    WhiteService ws;

    @Resource
    BlackService bs;


    @ApiOperation("查询群聊白名单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "white", value = "群聊白名单对象", dataType = "GroupWhite", paramType = "body", examples = @Example(value = {
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
    public TableDataInfo list(@RequestBody GroupWhite white) {
        return getDataTable(ws.list(white));
    }


    @ApiOperation("新增群聊白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "white", value = "群聊白名单对象", dataType = "GroupWhite", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "groupUid", value = "123456"),
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "新增成功")
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody GroupWhite white) {
        if (bs.isBlack(white.getGroupUid(), null)) {
            return toAjax(ws.save(white) != null);
        }
        return error(HttpCodeEnum.FAIL, I18nUtils.BWBlackExist());
    }


    @ApiOperation("删除群聊白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "群聊白名单ID", dataType = "Long", paramType = "path")
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
        ws.remove(id);
        return success();
    }

}
