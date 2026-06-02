package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.modules.warframe.service.WeaponService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 武器
 */
@RestController
@RequestMapping("/data/warframe/weapons")
public class WeaponsController extends BaseController {

    private final WeaponsRepository repository;
    private final WeaponService weaponService;
    private final ApplicationEventPublisher eventPublisher;

    public WeaponsController(WeaponsRepository repository, WeaponService weaponService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.weaponService = weaponService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Weapons entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.weapons", () -> weaponService.initFromExport());
        return success(I18nUtils.RequestTaskRun());
    }
}
