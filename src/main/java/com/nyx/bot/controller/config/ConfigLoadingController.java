package com.nyx.bot.controller.config;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {

    @GetMapping
    public AjaxResult loading() {
        return success().put("data", HandOff.getConfig());
    }

    @PostMapping
    public AjaxResult save(@Validated @RequestBody NyxConfig config) {
        if (!config.isValidateServerUrl()) {
            return error("服务端地址不符合规范！");
        }
        if (!config.isValidateClientUrl()) {
            return error("客户端地址不符合规范！");
        }
        return toAjax(HandOff.handoff(config));
    }

}
