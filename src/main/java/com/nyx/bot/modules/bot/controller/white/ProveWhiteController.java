package com.nyx.bot.modules.bot.controller.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.bot.entity.white.ProveWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {

    @Resource
    WhiteService whiteService;

    @Resource
    BlackService bs;

    @ApiOperation("查询个人白名单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pw", value = "个人白名单对象", dataType = "ProveWhite", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "proveUid", value = "123456"),
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
    public TableDataInfo list(@RequestBody ProveWhite proveWhite) {
        return getDataTable(whiteService.list(proveWhite));
    }

    @ApiOperation("添加个人白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pw", value = "个人白名单对象", dataType = "ProveWhite", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "botUid", value = "123456"),
                    @ExampleProperty(mediaType = "proveUid", value = "123456"),
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功", examples = @Example(value = {
                    @ExampleProperty(mediaType = "code", value = "200"),
                    @ExampleProperty(mediaType = "msg", value = "添加成功"),
            })),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody ProveWhite white) {
        if (bs.isBlack(null, white.getProveUid())) {
            return toAjax(whiteService.save(white) != null);
        }
        return error(HttpCodeEnum.FAIL, I18nUtils.BWBlackExist());
    }

    @ApiOperation("删除个人白名单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "个人白名单ID", dataType = "Long", paramType = "path", examples = @Example(value = {
                    @ExampleProperty(mediaType = "id", value = "123456"),
            }))
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
        whiteService.removeProve(id);
        return success();
    }
}
