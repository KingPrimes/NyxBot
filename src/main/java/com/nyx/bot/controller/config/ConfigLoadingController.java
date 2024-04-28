package com.nyx.bot.controller.config;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {

    String prefix = "config/config";


    @GetMapping()
    public String loading(Model map) {
        map.addAttribute("config", HandOff.getConfig());
        return prefix;
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(NyxConfig config) {
        return toAjax(HandOff.handoff(config));

    }

}
