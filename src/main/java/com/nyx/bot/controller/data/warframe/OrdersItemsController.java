package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {

    @Resource
    OrdersItemsRepository repository;


    @PostMapping("/list")
    public ResponseEntity<?> list(OrdersItems oi) {
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
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getMarket);
        return success("已执行更新操作！");
    }
}
