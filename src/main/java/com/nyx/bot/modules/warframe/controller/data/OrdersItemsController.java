package com.nyx.bot.modules.warframe.controller.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;


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
                        oi.getName(),
                        PageRequest.of(
                                oi.getCurrent() - 1, oi.getSize()
                        )
                )
        );
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::initOrdersItemsData);
        return success(I18nUtils.RequestTaskRun());
    }
}
