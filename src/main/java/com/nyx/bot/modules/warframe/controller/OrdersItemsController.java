package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.repo.OrdersItemsRepository;
import com.nyx.bot.modules.warframe.service.OrdersItemsService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 可交易物品列表
 */
@RestController
@RequestMapping("/data/warframe/market")
public class OrdersItemsController extends BaseController {

    private final OrdersItemsRepository repository;
    private final OrdersItemsService ordersItemsService;
    private final ApplicationEventPublisher eventPublisher;

    public OrdersItemsController(OrdersItemsRepository repository, OrdersItemsService ordersItemsService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.ordersItemsService = ordersItemsService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody OrdersItems oi) {
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
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.orders", () -> ordersItemsService.initOrdersItemsData());
        return success(I18nUtils.RequestTaskRun());
    }
}
