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

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody ProveWhite proveWhite) {
        return getDataTable(whiteService.list(proveWhite));
    }

    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody ProveWhite white) {
        if (bs.isBlack(null, white.getProveUid())) {
            return toAjax(whiteService.save(white) != null);
        }
        return error(HttpCodeEnum.FAIL, I18nUtils.BWBlackExist());
    }

    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.removeProve(id);
        return success();
    }
}
