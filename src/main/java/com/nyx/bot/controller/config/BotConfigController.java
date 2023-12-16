package com.nyx.bot.controller.config;

import com.nyx.bot.core.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/config")
public class BotConfigController extends BaseController {

    String prefix = "config/bot/";

    @GetMapping("/bot")
    public String config() {
        return prefix + "bot";
    }

}
