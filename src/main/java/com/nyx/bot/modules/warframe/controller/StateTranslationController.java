package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.enums.StateTypeEnum;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.service.StateTranslationService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Warframe 状态翻译
 */
@RestController
@RequestMapping("/data/warframe/state-translation")
public class StateTranslationController extends BaseController {

    private final StateTranslationRepository repository;
    private final StateTranslationService service;
    private final ApplicationEventPublisher eventPublisher;

    public StateTranslationController(StateTranslationRepository repository, StateTranslationService service, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.service = service;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody StateTranslation entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.state-translation", service::initData);
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Validated @RequestBody StateTranslation entity) {
        service.save(entity);
        return success();
    }

    @GetMapping("/edit/{uniqueName}")
    public ApiResponse<?> edit(@PathVariable String uniqueName) {
        Optional<StateTranslation> st = repository.findById(uniqueName);
        return st.isPresent() ? success(Map.of("data", st.get())) : success();
    }

    @DeleteMapping("/remove/{uniqueName}")
    public ApiResponse<Void> remove(@PathVariable String uniqueName) {
        repository.deleteById(uniqueName);
        return success();
    }

    @GetMapping("/types")
    public ApiResponse<?> types() {
        List<Map<String, String>> list = Arrays.stream(StateTypeEnum.values())
                .map(e -> Map.of("value", e.name(), "label", e.getNAME()))
                .toList();
        return success(list);
    }
}
