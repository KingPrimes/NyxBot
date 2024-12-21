package com.nyx.bot.controller.config.bot.white;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.bot.white.ProveWhite;
import com.nyx.bot.repo.impl.white.WhiteService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {

    @Resource
    WhiteService whiteService;

    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody ProveWhite proveWhite) {
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

    @PostMapping("/handoff")
    public AjaxResult handoff() {
        NyxConfig nyxConfig = HandOff.getConfig();
        nyxConfig.setIsBlackOrWhite(!nyxConfig.getIsBlackOrWhite());
        return toAjax(HandOff.handoff(nyxConfig));
    }
}
