package com.nyx.bot.modules.bot.controller.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.entity.black.ProveBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/black/prove")
public class ProveBlackController extends BaseController {

    @Resource
    BlackService bs;

    @ApiOperation("获取个人黑名单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pb", value = "个人黑名单对象", dataType = "ProveBlack", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "proveUid", value = "123456")
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "获取成功"),
                    @ExampleProperty(mediaType = "data",value = """
                            {
                                "total": 1,
                                "size": 10,
                                "current": 1,
                                "records": [
                                    {
                                        "id": 1,
                                        "botUid": 123456,
                                        "proveUid": 123456,
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
    public TableDataInfo list(@RequestBody ProveBlack pb) {
        return getDataTable(bs.list(pb));
    }

    @ApiOperation("添加个人黑名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pb", value = "个人黑名单对象", dataType = "ProveBlack", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "proveUid", value = "123456")
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "添加成功")
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody ProveBlack pb) {
        return toAjax(bs.save(pb));
    }

    @ApiOperation("删除个人黑名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "个人黑名单id", dataType = "Long", paramType = "path", examples = @Example(value = {
                    @ExampleProperty(mediaType = "id", value = "123456")
            }))
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
        return toAjax(bs.removeProve(id));
    }

}
