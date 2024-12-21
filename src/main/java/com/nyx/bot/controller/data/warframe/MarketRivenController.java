package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.RivenItems;
import com.nyx.bot.repo.warframe.RivenItemsRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> list(@RequestBody RivenItems rivenItems) {
        return getDataTable(
                repository.findAllPageable(
                        rivenItems.getItemName().isEmpty() ? null : rivenItems.getItemName(),
                        rivenItems.getRivenType().isEmpty() ? null : rivenItems.getRivenType(),
                        PageRequest.of(
                                rivenItems.getPageNum() - 1,
                                rivenItems.getPageSize())
                )
        );
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getRivenWeapons);
        return success("已执行任务！");
    }

}
