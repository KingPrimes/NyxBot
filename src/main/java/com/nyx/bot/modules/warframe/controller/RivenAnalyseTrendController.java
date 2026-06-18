package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.repo.RivenAnalyseTrendRepository;
import com.nyx.bot.modules.warframe.service.RivenAnalyseTrendService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 紫卡分析趋势
 */
@RestController
@RequestMapping("/data/warframe/riven-analyse")
public class RivenAnalyseTrendController extends BaseController {

    private final RivenAnalyseTrendRepository repository;
    private final RivenAnalyseTrendService rivenAnalyseTrendService;
    private final ApplicationEventPublisher eventPublisher;

    public RivenAnalyseTrendController(RivenAnalyseTrendRepository repository, RivenAnalyseTrendService rivenAnalyseTrendService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.rivenAnalyseTrendService = rivenAnalyseTrendService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody RivenAnalyseTrend entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.riven-analyse", rivenAnalyseTrendService::updateRivenAnalyseTrends);
        return success(I18nUtils.RequestTaskRun());
    }
}
