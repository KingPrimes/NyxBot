package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {
    String prefix = "data/warframe/";

    @Resource
    OrdersItemsRepository repository;


    @GetMapping
    public String market() {
        return prefix + "market";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity list(OrdersItems oi) {
        return getDataTable(
                repository.findAllPageable(
                        oi.getItemName(),
                        PageRequest.of(
                                oi.getPageNum() - 1, oi.getPageSize()
                        )
                )
        );
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getMarket);
        return success("已执行更新操作！");
    }
}
