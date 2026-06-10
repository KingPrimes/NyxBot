package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.Warframes;
import com.nyx.bot.modules.warframe.repo.exprot.WarframesRepository;
import com.nyx.bot.modules.warframe.service.WarframeService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 战甲
 */
@RestController
@RequestMapping("/data/warframe/warframes")
public class WarframesController extends BaseController {

    private final WarframesRepository repository;
    private final WarframeService warframeService;
    private final ApplicationEventPublisher eventPublisher;

    public WarframesController(WarframesRepository repository, WarframeService warframeService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.warframeService = warframeService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Warframes entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.warframes", warframeService::initFromExport);
        return success(I18nUtils.RequestTaskRun());
    }
}
