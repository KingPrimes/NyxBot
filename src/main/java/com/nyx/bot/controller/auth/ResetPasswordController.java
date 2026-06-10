package com.nyx.bot.controller.auth;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.I18nUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 重置密码
 */
@RestController
public class ResetPasswordController extends BaseController {

    private final UserService userService;

    private final SysUserRepository repository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    public ResetPasswordController(UserService userService, SysUserRepository repository, JwtUtil jwtUtil,
                                   PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.repository = repository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/restorePassword")
    public ApiResponse<Void> restPwd(Authentication authentication, @Validated @RequestBody ResetPassword params,
                                     HttpServletRequest request) {
        if (params.isValidOld()) return error(I18nUtils.ControllerRestPassWordON());
        if (!params.isValid()) return error(I18nUtils.ControllerRestPassWordONError());


        UserDetails userDetails = userService.loadUserByUsername(authentication.getName());
        if (!passwordEncoder.matches(params.getOldPassword(), userDetails.getPassword())) {
            return error(I18nUtils.ControllerRestPassWordOldError());
        }
        if (passwordEncoder.matches(params.getNewPassword(), userDetails.getPassword())) {
            return error(I18nUtils.ControllerRestPassWordON());
        }
        return repository.findSysUsersByUserName(userDetails.getUsername())
                .map(s -> {
                    s.setPassword(passwordEncoder.encode(params.getNewPassword()));
                    repository.save(s);
                    // 使当前令牌失效，强制重新登录
                    revokeToken(request);
                    return success();
                }).orElse(error(I18nUtils.ControllerRestPassWordONError()));
    }

    private void revokeToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("authorization");
            if (authHeader != null && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
                String token = authHeader.substring(Constants.TOKEN_PREFIX.length());
                String jti = jwtUtil.extractJti(token);
                long remainingSeconds = jwtUtil.getRemainingExpirationSeconds(token);
                if (jti != null && remainingSeconds > 0) {
                    CacheUtils.putWithExpiry(CacheUtils.TOKEN_BLACKLIST, jti, true,
                            remainingSeconds, TimeUnit.SECONDS);
                }
            }
        } catch (Exception ignored) {
            // 令牌解析失败时静默处理
        }
    }


    @Data
    public static class ResetPassword {
        @NotEmpty(message = "validated.OldPasswordNotEmpty")
        @Size(min = 6, max = 18, message = "validated.PasswordSize")
        private String oldPassword;

        @NotEmpty(message = "validated.NewPasswordNotEmpty")
        @Size(min = 6, max = 18, message = "validated.PasswordSize")
        private String newPassword;

        @NotEmpty(message = "validated.ConfirmPasswordNotEmpty")
        @Size(min = 6, max = 18, message = "validated.PasswordSize")
        private String confirmPassword;

        public boolean isValid() {
            return newPassword.equals(confirmPassword);
        }

        public boolean isValidOld() {
            return oldPassword.equals(newPassword);
        }

    }

}
