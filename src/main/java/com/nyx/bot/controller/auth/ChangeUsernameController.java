package com.nyx.bot.controller.auth;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.JwtUtil;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 更改用户名
 */
@RestController
public class ChangeUsernameController extends BaseController {

    private final UserService userService;

    private final SysUserRepository repository;

    private final JwtUtil jwtUtil;

    public ChangeUsernameController(UserService userService, SysUserRepository repository, JwtUtil jwtUtil) {
        this.userService = userService;
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/changeUsername")
    public ApiResponse<Void> changeUsername(Authentication authentication, @Validated @RequestBody ChangeUsername params,
                                            HttpServletRequest request) {
        if (params == null) {
            return error(I18nUtils.RequestErrorParam());
        }
        if (params.getNewUsername() == null || params.getNewUsername().isEmpty()) {
            return error(I18nUtils.Validated("NewNameNotEmpty"));
        }
        if (params.getNewUsername().length() < 4 || params.getNewUsername().length() > 20) {
            return error(I18nUtils.Validated("UserNameSize"));
        }
        if (params.getPassword() == null || params.getPassword().isEmpty()) {
            return error(I18nUtils.Validated("PasswordNotEmpty"));
        }

        // 获取当前用户信息
        UserDetails userDetails = userService.loadUserByUsername(authentication.getName());

        // 验证密码
        PasswordEncoder encoder = SpringUtils.getBean(PasswordEncoder.class);
        if (!encoder.matches(params.getPassword(), userDetails.getPassword())) {
            return error(I18nUtils.ControllerRestPassWordOldError());
        }
        // 检查新用户名是否已被占用
        if (repository.findSysUsersByUserName(params.getNewUsername()).isPresent()) {
            return error(I18nUtils.Validated("UserNameExists"));
        }
        // 更新用户名
        Optional<SysUser> sysUser = repository.findSysUsersByUserName(userDetails.getUsername());
        SysUser user = sysUser.map(s -> {
            s.setUserName(params.getNewUsername());
            return s;
        }).orElse(null);

        if (user == null) {
            return error(I18nUtils.Validated("UserNotExists"));
        }
        repository.save(user);

        // 使当前令牌失效，强制重新登录
        revokeToken(request);

        return success(I18nUtils.AuthSuccess("EditUserName"));
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
    public static class ChangeUsername {
        @NotEmpty(message = "validated.NewNameNotEmpty")
        @Size(min = 4, max = 20, message = "validated.UserNameSize")
        private String newUsername;

        @NotEmpty(message = "validated.PasswordNotEmpty")
        @Size(min = 6, max = 18, message = "validated.PasswordSize")
        private String password;
    }

}
