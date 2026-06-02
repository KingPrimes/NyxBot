package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.NightWave;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.service.NightWaveService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 电波
 */
@RestController
@RequestMapping("/data/warframe/night-wave")
public class NightWaveController extends BaseController {

    private final NightWaveRepository repository;
    private final NightWaveService nightWaveService;
    private final ApplicationEventPublisher eventPublisher;

    public NightWaveController(NightWaveRepository repository, NightWaveService nightWaveService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.nightWaveService = nightWaveService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody NightWave entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.night-wave", () -> nightWaveService.initFromExport());
        return success(I18nUtils.RequestTaskRun());
    }
}
