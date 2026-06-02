package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.service.RelicsService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Warframe 遗物
 */
@RestController
@RequestMapping("/data/warframe/relics")
public class RelicsController extends BaseController {

    private final RelicsService rs;
    private final ApplicationEventPublisher eventPublisher;

    public RelicsController(RelicsService rs, ApplicationEventPublisher eventPublisher) {
        this.rs = rs;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody Relics relics) {
        return ApiResponse.ok(rs.findAllPageable(relics));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update() {
        DataRefreshEvent.runAsync(eventPublisher, "data.refresh.task.relics", () -> rs.initRelicsData());
        return success(I18nUtils.RequestTaskRun());
    }

}
