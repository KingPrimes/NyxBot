package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.repo.impl.warframe.AliasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/alias")
public class AliasController extends BaseController {

    String prefix = "data/warframe/";

    @Autowired
    AliasService aliasService;


    @GetMapping
    public String alias() {
        return prefix + "alias";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Alias alias) {
        Page<Alias> list = aliasService.list(alias);
        return getDataTable(list.getContent(), list.getTotalElements());
    }


}
