package com.nyx.bot.modules.bot.controller.black;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.bot.entity.black.GroupBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 群聊黑名单
 */
@RestController
@RequestMapping("/config/bot/black/group")
public class GroupBlackController extends BaseController {

    private final BlackService bs;

    public GroupBlackController(BlackService bs) {
        this.bs = bs;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody GroupBlack gb) {
        return getDataTable(bs.list(gb));
    }

    @PostMapping("/save")
    public ApiResponse<Void> add(@Validated @RequestBody GroupBlack gb) {
        return toAjax(bs.save(gb));
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable("id") Long id) {
        return toAjax(bs.remove(id));
    }

}
