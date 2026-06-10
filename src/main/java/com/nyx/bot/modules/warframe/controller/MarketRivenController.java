package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.modules.warframe.service.RivenItemsService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 紫卡武器
 */
@RestController
@RequestMapping("/data/warframe/market/riven")

public class MarketRivenController extends BaseController {
    private final RivenItemsRepository repository;

    private final RivenItemsService rivenItemsService;

    private final ApplicationEventPublisher eventPublisher;

    public MarketRivenController(RivenItemsRepository repository, RivenItemsService rivenItemsService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.rivenItemsService = rivenItemsService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody RivenItems rivenItems) {
        return getDataTable(
                repository.findAllPageable(
                        rivenItems.getName(),
                        rivenItems.getRivenType(),
                        PageRequest.of(
                                rivenItems.getCurrent() - 1,
                                rivenItems.getSize())
                )
        );
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.riven", rivenItemsService::initRivenItemsData);
        return success(I18nUtils.RequestTaskRun());
    }

}
