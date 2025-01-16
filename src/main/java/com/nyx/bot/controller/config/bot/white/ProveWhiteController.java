package com.nyx.bot.controller.config.bot.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.white.ProveWhite;
import com.nyx.bot.repo.impl.white.WhiteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {

    @Resource
    WhiteService whiteService;

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody ProveWhite proveWhite) {
        return getDataTable(whiteService.list(proveWhite));
    }

    @PostMapping("/save")
    public AjaxResult add(@RequestBody ProveWhite white) {
        if (white == null) return error();
        return toAjax(whiteService.save(white) != null);
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable("id") Long id) {
        return success().put("white", whiteService.findByProve(id));
    }


    @PostMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.removeProve(id);
        return success();
    }
}
