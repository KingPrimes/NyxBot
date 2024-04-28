package com.nyx.bot.controller.config.bot.black;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.bot.black.ProveBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/config/bot/black/prove")
public class ProveBlackController extends BaseController {
    String prefix = "config/bot/black/prove";

    @Resource
    BlackService bs;

    @GetMapping
    public String prove() {
        return prefix + "/prove";
    }


    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(ProveBlack pb) {
        return getDataTable(bs.list(pb));
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ProveBlack pb) {
        return toAjax(bs.save(pb));
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model map) {
        map.addAttribute("black", bs.findByProveId(id));
        return prefix + "/edit";
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult edit(ProveBlack pb) {
        return toAjax(bs.save(pb));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.removeProve(id));
    }

    @PostMapping("/handoff")
    @ResponseBody
    public AjaxResult handoff() {
        NyxConfig nyxConfig = HandOff.getConfig();
        nyxConfig.setIsBlackOrWhite(!nyxConfig.getIsBlackOrWhite());
        return toAjax(HandOff.handoff(nyxConfig));
    }

}
