package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.Nodes;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.service.NodeService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Warframe 节点
 */
@RestController
@RequestMapping("/data/warframe/nodes")
public class NodesController extends BaseController {

    private final NodesRepository repository;
    private final NodeService nodeService;
    private final ApplicationEventPublisher eventPublisher;

    public NodesController(NodesRepository repository, NodeService nodeService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.nodeService = nodeService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Nodes entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.nodes", () -> nodeService.initData());
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@RequestBody Nodes entity) {
        repository.save(entity);
        return success();
    }

    @GetMapping("/edit/{uniqueName}")
    public ApiResponse<?> edit(@PathVariable String uniqueName) {
        Optional<Nodes> node = repository.findById(uniqueName);
        return node.isPresent() ? success(Map.of("data", node.get())) : success();
    }

    @DeleteMapping("/remove/{uniqueName}")
    public ApiResponse<Void> remove(@PathVariable String uniqueName) {
        repository.deleteById(uniqueName);
        return success();
    }
}
