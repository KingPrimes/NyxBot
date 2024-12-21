package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Ephemeras;
import com.nyx.bot.repo.warframe.EphemerasRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    @Resource
    EphemerasRepository ephemerasRepository;

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(@RequestBody Ephemeras e) {
        return getDataTable(ephemerasRepository.findAllPageable(
                e.getItemName(),
                PageRequest.of(
                        e.getPageNum() - 1,
                        e.getPageSize())
        ));
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.runAsync(WarframeDataSource::getEphemeras);
        return success("已执行任务！");
    }
}
