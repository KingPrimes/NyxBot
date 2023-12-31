package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/translation")
public class TranslationController extends BaseController {
    String prefix = "data/warframe/";

    @Autowired
    TranslationService tlService;


    @GetMapping
    public String alias() {
        return prefix + "translation";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Translation t) {
        Page<Translation> list = tlService.list(t);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.initTranslation();
        return success("已执行任务！");
    }
}
