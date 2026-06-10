package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.Alias;
import com.nyx.bot.modules.warframe.repo.AliasRepository;
import com.nyx.bot.modules.warframe.service.AliasService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Warframe 别名
 */
@RestController
@RequestMapping("/data/warframe/alias")

public class AliasController extends BaseController {

    private final AliasRepository repository;

    private final AliasService aliasService;

    private final ApplicationEventPublisher eventPublisher;

    public AliasController(AliasRepository repository, AliasService aliasService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.aliasService = aliasService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Alias alias) {
        return getDataTable(repository.findByLikeCn(alias.getCn(), PageRequest.of(alias.getCurrent() - 1, alias.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.alias", aliasService::updateAlias);
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Validated @RequestBody Alias a) {
        if (!a.isValidEnglish()) {
            return error(I18nUtils.message("request.valid.alias.en"));
        }
        if (!a.isValidChinese()) {
            return error(I18nUtils.message("request.valid.alias.ch"));
        }
        Optional<Alias> alias = repository.findByCnAndEn(a.getCn(), a.getEn());
        if (alias.isPresent()) {
            return error(I18nUtils.message("request.error.data.already.exists"));
        }
        repository.save(a);
        return success();
    }

    @GetMapping("/edit/{id}")
    public ApiResponse<?> edit(@PathVariable Long id) {
        Optional<Alias> alias = repository.findById(id);
        if (alias.isPresent()) {
            return success(Map.of("alias", alias.get()));
        }
        return success();
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        repository.deleteById(id);
        return success();
    }
}
