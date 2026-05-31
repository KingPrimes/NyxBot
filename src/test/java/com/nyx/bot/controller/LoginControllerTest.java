package com.nyx.bot.controller;


import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.modules.system.entity.SysUser;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void login_Success() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setPassword("admin123");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.withUsername("admin").password("admin123").authorities("ROLE_USER").build();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        doReturn("testToken").when(jwtUtil).generateToken("admin");

        // Act & Assert
        assertDoesNotThrow(() -> {
            ApiResponse<?> result = loginController.login(user, request);

            assertTrue(result.isSuccess());
            assertEquals("登录成功", result.getMsg());
            assertEquals("testToken", ((Map<?, ?>) result.getData()).get("token"));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtil).generateToken("admin");
        });
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setPassword("admin123111");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // Act
        ApiResponse<?> result = loginController.login(user, request);

        assertEquals("User not found", result.getMsg());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_NullUser() {
        assertThrows(Exception.class, () -> loginController.login(null, request));
    }

    @Test
    void login_EmptyUsername() {
        SysUser user = new SysUser();
        user.setUserName("");
        user.setPassword("testPass");

        assertThrows(Exception.class, () -> loginController.login(user, request));
    }

    @Test
    void login_EmptyPassword() {
        SysUser user = new SysUser();
        user.setUserName("testUser");
        user.setPassword("");

        assertThrows(Exception.class, () -> loginController.login(user, request));
    }
}
