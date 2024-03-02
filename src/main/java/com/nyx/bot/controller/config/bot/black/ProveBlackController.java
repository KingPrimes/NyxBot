package com.nyx.bot.controller.config.bot.black;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.black.ProveBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
    public TableDataInfo list(ProveBlack pb) {
        Page<ProveBlack> list = bs.list(pb);
        return getDataTable(list.getContent(), list.getTotalElements());
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
    public String edit(@PathVariable("id") Long id, ModelMap map) {
        map.put("black", bs.findByProveId(id));
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
        ProveBlack pb = new ProveBlack();
        pb.setId(id);
        return toAjax(bs.remove(pb));
    }

    @PostMapping("/handoff")
    @ResponseBody
    public AjaxResult handoff() {
        return SpringUtils.getBean(HandOff.class).handoff();
    }

}
