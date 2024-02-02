package com.nyx.bot.controller.config.black;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.black.PriveBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/config/black/prive")
public class PriveBlackController extends BaseController {
    String prefix = "config/black/prive";

    @Resource
    BlackService bs;

    @GetMapping
    public String prive() {
        return prefix + "/prive";
    }


    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(PriveBlack pb) {
        Page<PriveBlack> list = bs.list(pb);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(PriveBlack pb) {
        return toAjax(bs.save(pb));
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap map) {
        map.put("black", bs.findByPriveId(id));
        return prefix + "/edit";
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult edit(PriveBlack pb) {
        return toAjax(bs.save(pb));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("id") Long id) {
        PriveBlack pb = new PriveBlack();
        pb.setId(id);
        return toAjax(bs.remove(pb));
    }

}
