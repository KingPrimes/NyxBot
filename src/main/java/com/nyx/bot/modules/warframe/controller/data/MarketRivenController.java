package com.nyx.bot.modules.warframe.controller.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.RivenItems;
import com.nyx.bot.modules.warframe.repo.RivenItemsRepository;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/market/riven")
public class MarketRivenController extends BaseController {
    @Resource
    RivenItemsRepository repository;

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody RivenItems rivenItems) {
        return getDataTable(
                repository.findAllPageable(
                        rivenItems.getItemName(),
                        rivenItems.getRivenType(),
                        PageRequest.of(
                                rivenItems.getCurrent() - 1,
                                rivenItems.getSize())
                )
        );
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getRivenWeapons);
        return success(I18nUtils.RequestTaskRun());
    }

}
