package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.service.RelicsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/warframe/relics")
public class RelicsController extends BaseController {

    @Resource
    RelicsService rs;


    @PostMapping("/list")
    public TableDataInfo list(@RequestBody Relics relics) {
        return rs.findAllPageable(relics);
    }


}
