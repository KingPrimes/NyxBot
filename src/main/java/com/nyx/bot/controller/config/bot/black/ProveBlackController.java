package com.nyx.bot.controller.config.bot.black;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.bot.black.ProveBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/black/prove")
public class ProveBlackController extends BaseController {

    @Resource
    BlackService bs;

    @PostMapping("/list")
    public ResponseEntity<?> list(ProveBlack pb) {
        return getDataTable(bs.list(pb));
    }

    @PostMapping("/save")
    public AjaxResult add(ProveBlack pb) {
        if (pb == null) return error();
        return toAjax(bs.save(pb));
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable("id") Long id) {
        return AjaxResult.success().put("black", bs.findByProveId(id));
    }

    @PostMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.removeProve(id));
    }

    @PostMapping("/handoff")
    public AjaxResult handoff() {
        NyxConfig nyxConfig = HandOff.getConfig();
        nyxConfig.setIsBlackOrWhite(!nyxConfig.getIsBlackOrWhite());
        return toAjax(HandOff.handoff(nyxConfig));
    }

}
