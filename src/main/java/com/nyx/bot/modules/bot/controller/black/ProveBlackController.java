package com.nyx.bot.modules.bot.controller.black;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.bot.entity.black.ProveBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人黑名单
 */
@RestController
@RequestMapping("/config/bot/black/prove")
public class ProveBlackController extends BaseController {

    private final BlackService bs;

    public ProveBlackController(BlackService bs) {
        this.bs = bs;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody ProveBlack pb) {
        return getDataTable(bs.list(pb));
    }

    @PostMapping("/save")
    public ApiResponse<Void> add(@Validated @RequestBody ProveBlack pb) {
        return toAjax(bs.save(pb));
    }


    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable("id") Long id) {
        return toAjax(bs.removeProve(id));
    }

}
