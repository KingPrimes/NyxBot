package com.nyx.bot.controller.config.bot.white;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.white.ProveWhite;
import com.nyx.bot.repo.impl.white.WhiteService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/config/bot/white/prove")
public class ProveWhiteController extends BaseController {
    String prefix = "config/bot/white/prove";

    @Resource
    WhiteService whiteService;

    @GetMapping
    public String prove() {
        return prefix + "/prove";
    }


    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(ProveWhite proveWhite) {
        Page<ProveWhite> list = whiteService.list(proveWhite);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(ProveWhite white) {
        whiteService.save(white);
        return success();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap map) {
        map.put("white", whiteService.findByProve(id));
        return prefix + "/edit";
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult edit(ProveWhite white) {
        whiteService.save(white);
        return success();
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.removeProve(id);
        return success();
    }

    @PostMapping("/handoff")
    @ResponseBody
    public AjaxResult handoff() {
        return SpringUtils.getBean(HandOff.class).handoff();
    }
}
