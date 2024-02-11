package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
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
@RequestMapping("/data/warframe/rivenTrend")
public class RivenTrendController extends BaseController {
    String prefix = "data/warframe/rivenTrend/";

    @Resource
    RivenTrendService rtService;


    @GetMapping
    public String market() {
        return prefix + "rivenTrend";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(RivenTrend rt) {
        Page<RivenTrend> list = rtService.list(rt);
        return getDataTable(list.getContent(), list.getTotalElements());
    }


    @PostMapping("/update")
    public AjaxResult update() {
        WarframeDataSource.getRivenTrend(ApiUrl.WARFRAME_DATA_SOURCE_GIT_HUB);
        return success("已执行任务！");
    }

}
