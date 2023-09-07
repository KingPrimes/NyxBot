package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.repo.impl.warframe.OrdersItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {
    String prefix = "data/warframe/";

    @Autowired
    OrdersItemsService oiService;


    @GetMapping
    public String market() {
        return prefix + "market";
    }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(OrdersItems oi) {
        Page<OrdersItems> list = oiService.list(oi);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.getMarket();
        return success("已执行更新操作！");
    }
}
