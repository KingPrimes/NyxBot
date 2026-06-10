package com.nyx.bot.controller.config;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.controller.bot.HandOff;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置
 */
@RestController
@RequestMapping("/config/loading")
public class ConfigLoadingController extends BaseController {


    @GetMapping
    public ApiResponse<Object> loading() {
        return success(HandOff.getConfig());
    }


    @PostMapping
    public ApiResponse<Void> save(@Validated @RequestBody NyxConfig config) {
        if (!config.isValidateServerUrl()) {
            return error(I18nUtils.RequestValidServerUrl());
        }
        if (!config.isValidateClientUrl()) {
            return error(I18nUtils.RequestValidClientUrl());
        }
        boolean result = HandOff.handoff(config);
        // 同步更新 Spring Environment 中的 pluginPrefix，使运行时修改即时生效
        if (result) {
            ConfigurableEnvironment env = SpringUtils.getBean(ConfigurableEnvironment.class);
            PropertySource<?> dynamicPort = env.getPropertySources().get("dynamicPort");
            if (dynamicPort instanceof MapPropertySource propertySource) {
                propertySource.getSource().put("nyx.plugin-prefix", config.getPluginPrefix());
                propertySource.getSource().put("shiro.token", config.getToken());
            }
        }
        return toAjax(result);
    }

}
