package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.RivenTionAlias;
import com.nyx.bot.modules.warframe.repo.RivenTionAliasRepository;
import com.nyx.bot.modules.warframe.service.RivenTionAliasService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Warframe 紫卡词条别名
 */
@RestController
@RequestMapping("/data/warframe/riven-tion-alias")
public class RivenTionAliasController extends BaseController {

    private final RivenTionAliasRepository repository;
    private final RivenTionAliasService rivenTionAliasService;
    private final ApplicationEventPublisher eventPublisher;

    public RivenTionAliasController(RivenTionAliasRepository repository, RivenTionAliasService rivenTionAliasService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.rivenTionAliasService = rivenTionAliasService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody RivenTionAlias entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.riven-tion-alias", rivenTionAliasService::updateRivenTionAlias);
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Validated @RequestBody RivenTionAlias entity) {
        repository.save(entity);
        return success();
    }

    @GetMapping("/edit/{id}")
    public ApiResponse<?> edit(@PathVariable Long id) {
        Optional<RivenTionAlias> alias = repository.findById(id);
        return alias.isPresent() ? success(Map.of("data", alias.get())) : success();
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        repository.deleteById(id);
        return success();
    }
}
