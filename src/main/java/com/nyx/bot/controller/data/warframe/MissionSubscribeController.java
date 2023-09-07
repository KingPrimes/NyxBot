package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.impl.warframe.MissionSubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/subscribe")
public class MissionSubscribeController extends BaseController {
    String prefix = "data/warframe/";

    @Autowired
    MissionSubscribeService msService;


    @GetMapping
    public String subscribe(ModelMap map) {
        map.put("sub", SubscribeEnums.values());
        return prefix + "subscribe";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(MissionSubscribe ms) {
        Page<MissionSubscribe> list = msService.list(ms);

        return getDataTable(list.getContent(), list.getTotalElements());
    }

}
