package com.nyx.bot.controller;


import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.modules.system.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setPassword("admin123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        UserDetails userDetails = User.withUsername("admin").password("admin123").authorities("ROLE_USER").build();

        // 使用doReturn模式替代when模式，可以解决某些模拟问题
        doReturn(userDetails).when(userDetailsService).loadUserByUsername("admin");

        // 使用doReturn模式替代when模式
        doReturn("testToken").when(jwtUtil).generateToken("admin");

        // Act & Assert
        assertDoesNotThrow(() -> { // 添加异常捕获
            AjaxResult result = loginController.login(user);

            assertTrue(result.isSuccess()); // 验证登录成功
            assertEquals("登录成功", result.getMsg()); // 验证消息
            assertEquals("testToken", ((Map<?, ?>) result.get("data")).get("token")); //验证令牌

            // Verify interactions
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class)); //验证认证管理器调用
            verify(userDetailsService).loadUserByUsername("admin"); //验证用户详情服务调用
            verify(jwtUtil).generateToken("admin"); //验证JWT工具调用
        });

    }

    @Test
    void login_UserNotFound() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setPassword("admin123111");

        // 模拟认证失败抛出异常
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act
        AjaxResult result = loginController.login(user);

        assertEquals("User not found", result.get("msg"));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userDetailsService, jwtUtil); //确保未调用其他服务
    }

    @Test
    void login_NullUser() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginController.login(null));
    }

    @Test
    void login_EmptyUsername() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("");
        user.setPassword("testPass");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginController.login(user));
    }

    @Test
    void login_EmptyPassword() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("testUser");
        user.setPassword("");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginController.login(user));
    }
}
