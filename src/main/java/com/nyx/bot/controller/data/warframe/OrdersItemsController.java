package com.nyx.bot.controller.data.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.OrdersItems;
import com.nyx.bot.repo.warframe.OrdersItemsRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {

    @Resource
    OrdersItemsRepository repository;


    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody OrdersItems oi) {
        return getDataTable(
                repository.findAllPageable(
                        oi.getItemName(),
                        PageRequest.of(
                                oi.getCurrent() - 1, oi.getSize()
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
