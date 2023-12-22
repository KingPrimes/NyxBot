package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Ephemeras;
import com.nyx.bot.repo.impl.warframe.EphemerasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    String prefix = "data/warframe/";

    @Autowired
    EphemerasService epService;


    @GetMapping
    public String alias() {
        return prefix + "ephemeras";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Ephemeras e) {
        Page<Ephemeras> list = epService.list(e);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.getEphemeras();
        return success("已执行任务！");
    }
}
