package com.nyx.bot.common.config;

/**
 * locate.yaml 配置项 Key 常量池。
 * <p>
 * 所有 YAML key 在此集中定义，新增配置项只需加一个常量。
 * </p>
 *
 * @author KingPrimes
 */
public final class ConfigConstants {

    private ConfigConstants() { /* 工具类 */ }

    public static final String SERVER_PORT         = "serverPort";
    public static final String IS_SERVER_OR_CLIENT = "isServerOrClient";
    public static final String WS_SERVER_URL       = "wsServerUrl";
    public static final String WS_CLIENT_URL       = "wsClientUrl";
    public static final String TOKEN               = "token";
    public static final String HTTP_PROXY          = "httpProxy";
    public static final String SOCKS_PROXY         = "socksProxy";
    public static final String PROXY_USER          = "proxyUser";
    public static final String PROXY_PASSWORD      = "proxyPassword";
    public static final String PLUGIN_PREFIX       = "pluginPrefix";
    public static final String PLUGIN_NAME         = "pluginName";
}
