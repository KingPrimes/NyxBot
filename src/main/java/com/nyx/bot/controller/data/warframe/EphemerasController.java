package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Ephemeras;
import com.nyx.bot.repo.warframe.EphemerasRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/ephemeras")
public class EphemerasController extends BaseController {
    String prefix = "data/warframe/";

    @Resource
    EphemerasRepository ephemerasRepository;


    @GetMapping
    public String alias() {
        return prefix + "ephemeras";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity list(Ephemeras e) {
        return getDataTable(ephemerasRepository.findAllPageable(
                e.getItemName(),
                PageRequest.of(
                        e.getPageNum() - 1,
                        e.getPageSize())
        ));
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.getEphemeras();
        return success("已执行任务！");
    }
}
