package com.nyx.bot.controller.config;

import com.nyx.bot.common.config.ConfigConstants;
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

import java.util.Map;

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
        Map<String, Object> data = yamlService.load();
        NyxConfig config = new NyxConfig();
        config.setServerPort(intVal(data, ConfigConstants.SERVER_PORT, 8080));
        config.setIsServerOrClient(boolVal(data, ConfigConstants.IS_SERVER_OR_CLIENT, true));
        config.setWsServerUrl(strVal(data, ConfigConstants.WS_SERVER_URL, "/ws/shiro"));
        config.setWsClientUrl(strVal(data, ConfigConstants.WS_CLIENT_URL, "ws://localhost:3001"));
        config.setToken(strVal(data, ConfigConstants.TOKEN, ""));
        config.setHttpProxy(strVal(data, ConfigConstants.HTTP_PROXY, ""));
        config.setSocksProxy(strVal(data, ConfigConstants.SOCKS_PROXY, ""));
        config.setProxyUser(strVal(data, ConfigConstants.PROXY_USER, ""));
        config.setProxyPassword(strVal(data, ConfigConstants.PROXY_PASSWORD, ""));
        config.setPluginPrefix(boolVal(data, ConfigConstants.PLUGIN_PREFIX, false));
        config.setPluginName(strVal(data, ConfigConstants.PLUGIN_NAME, ""));
        return success(config);
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
        yamlService.update(data -> {
            putIfNonNull(data, ConfigConstants.SERVER_PORT, config.getServerPort());
            putIfNonNull(data, ConfigConstants.IS_SERVER_OR_CLIENT, config.getIsServerOrClient());
            putIfNonNull(data, ConfigConstants.WS_SERVER_URL, config.getWsServerUrl());
            putIfNonNull(data, ConfigConstants.WS_CLIENT_URL, config.getWsClientUrl());
            putIfNonNull(data, ConfigConstants.TOKEN, config.getToken());
            putIfNonNull(data, ConfigConstants.HTTP_PROXY, config.getHttpProxy());
            putIfNonNull(data, ConfigConstants.SOCKS_PROXY, config.getSocksProxy());
            putIfNonNull(data, ConfigConstants.PROXY_USER, config.getProxyUser());
            putIfNonNull(data, ConfigConstants.PROXY_PASSWORD, config.getProxyPassword());
            putIfNonNull(data, ConfigConstants.PLUGIN_PREFIX, config.getPluginPrefix());
            putIfNonNull(data, ConfigConstants.PLUGIN_NAME, config.getPluginName());
        });

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

    // ======== 类型安全读取辅助方法 ========

    private static String strVal(Map<String, Object> data, String key, String def) {
        Object v = data.get(key);
        return v instanceof String s ? s : def;
    }

    private static Integer intVal(Map<String, Object> data, String key, Integer def) {
        Object v = data.get(key);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static Boolean boolVal(Map<String, Object> data, String key, Boolean def) {
        Object v = data.get(key);
        if (v instanceof Boolean b) return b;
        return def;
    }

    private static void putIfNonNull(Map<String, Object> data, String key, Object value) {
        if (value != null) {
            data.put(key, value);
        }
    }
}
