package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.reward.RewardPool;
import com.nyx.bot.modules.warframe.repo.exprot.reward.RewardPoolRepository;
import com.nyx.bot.modules.warframe.service.RewardPoolService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Warframe 奖励池
 */
@RestController
@RequestMapping("/data/warframe/reward-pool")
public class RewardPoolController extends BaseController {

    private final RewardPoolRepository repository;
    private final RewardPoolService rewardPoolService;
    private final ApplicationEventPublisher eventPublisher;

    public RewardPoolController(RewardPoolRepository repository, RewardPoolService rewardPoolService, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.rewardPoolService = rewardPoolService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody RewardPool entity) {
        return getDataTable(repository.findAll(PageRequest.of(entity.getCurrent() - 1, entity.getSize())));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.reward-pool", () -> rewardPoolService.initRewardPool());
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@RequestBody RewardPool entity) {
        repository.save(entity);
        return success();
    }

    @GetMapping("/edit/{uniqueName}")
    public ApiResponse<?> edit(@PathVariable String uniqueName) {
        Optional<RewardPool> rp = repository.findById(uniqueName);
        return rp.isPresent() ? success(Map.of("data", rp.get())) : success();
    }

    @DeleteMapping("/remove/{uniqueName}")
    public ApiResponse<Void> remove(@PathVariable String uniqueName) {
        repository.deleteById(uniqueName);
        return success();
    }
}
