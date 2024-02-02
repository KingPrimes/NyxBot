package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.repo.impl.warframe.RivenTrendService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/rivent")
public class RivenTrendController extends BaseController {
    String prefix = "data/warframe/";

    @Resource
    RivenTrendService rtService;


    @GetMapping
    public String market() {
        return prefix + "rivent";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(RivenTrend rt) {
        Page<RivenTrend> list = rtService.list(rt);
        return getDataTable(list.getContent(), list.getTotalElements());
    }
}
