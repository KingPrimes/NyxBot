package com.nyx.bot.modules.bot.controller.white;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.bot.entity.white.ProveWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 个人白名单
 */
@RestController
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {

    private final WhiteService whiteService;

    private final BlackService bs;

    public ProveWhiteController(WhiteService whiteService, BlackService bs) {
        this.whiteService = whiteService;
        this.bs = bs;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody ProveWhite proveWhite) {
        return getDataTable(whiteService.list(proveWhite));
    }

    @PostMapping("/save")
    public ApiResponse<Void> add(@Validated @RequestBody ProveWhite white) {
        if (bs.isBlack(null, white.getProveUid())) {
            return toAjax(whiteService.save(white) != null);
        }
        return error(HttpStatus.BAD_REQUEST, I18nUtils.BWBlackExist());
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable("id") Long id) {
        whiteService.removeProve(id);
        return success();
    }
}
