package com.nyx.bot.modules.warframe.controller.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.modules.warframe.entity.Ephemeras;
import com.nyx.bot.modules.warframe.repo.EphemerasRepository;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    @Resource
    EphemerasRepository ephemerasRepository;

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody Ephemeras e) {
        return getDataTable(ephemerasRepository.findAllPageable(
                e.getItemName(),
                PageRequest.of(
                        e.getCurrent() - 1,
                        e.getSize())
        ));
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getEphemeras);
        return success(I18nUtils.RequestTaskRun());
    }
}
