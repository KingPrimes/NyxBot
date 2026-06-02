package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.RivenTion;
import com.nyx.bot.modules.warframe.repo.RivenTionRepository;
import com.nyx.bot.modules.warframe.service.RivenTionService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Warframe 紫卡词条
 */
@RestController
@RequestMapping("/data/warframe/riven-tion")
public class RivenTionController extends BaseController {

    private final RivenTionRepository repository;
    private final RivenTionService rivenTionService;
    private final ApplicationEventPublisher eventPublisher;

    public RivenTionController(RivenTionRepository repository, RivenTionService rivenTionService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.rivenTionService = rivenTionService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody RivenTion entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.riven-tion", () -> rivenTionService.updateRivenTion());
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Validated @RequestBody RivenTion entity) {
        repository.save(entity);
        return success();
    }

    @GetMapping("/edit/{id}")
    public ApiResponse<?> edit(@PathVariable Long id) {
        Optional<RivenTion> rivenTion = repository.findById(id);
        return rivenTion.isPresent() ? success(Map.of("data", rivenTion.get())) : success();
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        repository.deleteById(id);
        return success();
    }
}
