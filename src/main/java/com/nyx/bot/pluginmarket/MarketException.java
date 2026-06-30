package com.nyx.bot.pluginmarket;

/**
 * 插件市场操作异常。
 * <p>
 * 由 {@link PluginMarketController} 捕获并转为 {@code ApiResponse.error()} 返回。
 * </p>
 *
 * @author KingPrimes
 */
public class MarketException extends RuntimeException {

    public MarketException(String message) {
        super(message);
    }
}
