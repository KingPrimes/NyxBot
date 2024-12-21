package com.nyx.bot.controller.config;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {

    @GetMapping()
    public AjaxResult loading() {
        return success().put("config", HandOff.getConfig());
    }

    @PostMapping("/save")
    public AjaxResult save(@RequestBody NyxConfig config) {
        return toAjax(HandOff.handoff(config));

    }

}
