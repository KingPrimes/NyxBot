package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.Ephemeras;
import com.nyx.bot.modules.warframe.repo.EphemerasRepository;
import com.nyx.bot.modules.warframe.service.EphemerasService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 幻纹
 */
@RestController
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    private final EphemerasRepository ephemerasRepository;

    private final EphemerasService ephemerasService;

    private final ApplicationEventPublisher eventPublisher;

    public EphemerasController(EphemerasRepository ephemerasRepository, EphemerasService ephemerasService, ApplicationEventPublisher eventPublisher) {
        this.ephemerasRepository = ephemerasRepository;
        this.ephemerasService = ephemerasService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Ephemeras e) {
        return getDataTable(ephemerasRepository.findAllPageable(
                e.getName(),
                PageRequest.of(
                        e.getCurrent() - 1,
                        e.getSize())
        ));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.ephemeras", ephemerasService::initEphemerasData);
        return success(I18nUtils.RequestTaskRun());
    }
}
