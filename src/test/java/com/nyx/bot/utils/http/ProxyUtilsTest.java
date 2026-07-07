package com.nyx.bot.utils.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("ProxyUtils 单元测试")
class ProxyUtilsTest {

    @Test
    @DisplayName("Windows ProxyServer 支持带协议的地址")
    void testParseWindowsProxyServerWithScheme() {
        Proxy proxy = ProxyUtils.parseWindowsProxyServer("http://127.0.0.1:7890");

        assertProxy(proxy, "127.0.0.1", 7890);
    }

    @Test
    @DisplayName("Windows ProxyServer 支持 host:port 地址")
    void testParseWindowsProxyServerHostPort() {
        Proxy proxy = ProxyUtils.parseWindowsProxyServer("127.0.0.1:7890");

        assertProxy(proxy, "127.0.0.1", 7890);
    }

    @Test
    @DisplayName("Windows ProxyServer 支持按协议分组的地址")
    void testParseWindowsProxyServerPerProtocol() {
        Proxy proxy = ProxyUtils.parseWindowsProxyServer("socks=127.0.0.1:7891;http=127.0.0.1:7890;https=127.0.0.1:7890");

        assertProxy(proxy, "127.0.0.1", 7890);
    }

    @Test
    @DisplayName("Windows ProxyServer 非法地址返回 NO_PROXY")
    void testParseWindowsProxyServerInvalid() {
        assertEquals(Proxy.NO_PROXY, ProxyUtils.parseWindowsProxyServer("http://127.0.0.1"));
    }

    private static void assertProxy(Proxy proxy, String host, int port) {
        assertNotEquals(Proxy.NO_PROXY, proxy);
        assertEquals(Proxy.Type.HTTP, proxy.type());
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertEquals(host, address.getHostString());
        assertEquals(port, address.getPort());
    }
}
