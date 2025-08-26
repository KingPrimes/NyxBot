package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import com.nyx.bot.utils.I18nUtils;
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
            return error(I18nUtils.RequestValidServerUrl());
        }
        if (!config.isValidateClientUrl()) {
            return error(I18nUtils.RequestValidClientUrl());
        }
        return toAjax(HandOff.handoff(config));
    }

}
