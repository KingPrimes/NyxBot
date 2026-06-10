package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.repo.LichSisterWeaponsRepository;
import com.nyx.bot.modules.warframe.service.LichSisterWeaponsService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 赤毒/信条武器
 */
@RestController
@RequestMapping("/data/warframe/lich-sister")
public class LichSisterWeaponsController extends BaseController {

    private final LichSisterWeaponsRepository repository;
    private final LichSisterWeaponsService lichSisterWeaponsService;
    private final ApplicationEventPublisher eventPublisher;

    public LichSisterWeaponsController(LichSisterWeaponsRepository repository, LichSisterWeaponsService lichSisterWeaponsService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.lichSisterWeaponsService = lichSisterWeaponsService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody LichSisterWeapons entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.lich-sister", lichSisterWeaponsService::initLichSisterWeaponsData);
        return success(I18nUtils.RequestTaskRun());
    }
}
