package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.Relics;
import com.nyx.bot.repo.impl.warframe.RelicsService;
import com.nyx.bot.repo.warframe.RelicsRewardsRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data/warframe/relics")
public class RelicsController extends BaseController {

    @Resource
    RelicsService rs;

    @Resource
    RelicsRewardsRepository rewardsRepository;


    @PostMapping("/list")
    public TableDataInfo list(Relics relics) {
        return rs.findAllPageable(relics);
    }


}
