package com.nyx.bot.controller.config;

import com.nyx.bot.common.config.LocateYamlService;
import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.NyxConfig;
import com.nyx.bot.common.core.controller.BaseController;
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

    private final LocateYamlService yamlService;

    public ConfigLoadingController(LocateYamlService yamlService) {
        this.yamlService = yamlService;
    }

    @GetMapping
    public ApiResponse<Object> loading() {
        return success(NyxConfig.fromMap(yamlService.load()));
    }

    @PostMapping
    public ApiResponse<Void> save(@Validated @RequestBody NyxConfig config) {
        if (!config.isValidateServerUrl()) {
            return error(I18nUtils.RequestValidServerUrl());
        }
        if (!config.isValidateClientUrl()) {
            return error(I18nUtils.RequestValidClientUrl());
        }
        // 原子更新配置，避免并发丢失更新
        yamlService.update(config::mergeInto);

        // 同步更新 Spring Environment，使运行时修改即时生效（仅非 null 字段）
        ConfigurableEnvironment env = SpringUtils.getBean(ConfigurableEnvironment.class);
        PropertySource<?> dynamicPort = env.getPropertySources().get("dynamicPort");
        if (dynamicPort instanceof MapPropertySource propertySource) {
            if (config.getPluginPrefix() != null) {
                propertySource.getSource().put("nyx.plugin-prefix", config.getPluginPrefix());
            }
            if (config.getToken() != null) {
                propertySource.getSource().put("shiro.token", config.getToken());
            }
        }
        return toAjax(true);
    }
}
