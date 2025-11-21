package com.nyx.bot.controller.auth;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 更改用户名
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT"
)
@Tag(name = "auth.username", description = "更改用户名")
@SecurityRequirement(name = "Bearer")
@RestController
public class ChangeUsernameController extends BaseController {

    @Resource
    UserService userService;

    @Resource
    SysUserRepository repository;

    @Operation(
            summary = "更改用户名",
            description = "更改用户名",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "更改用户名参数",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ChangeUsername.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "newUsername": "newUser",
                                                        "password": "123456"
                                                    }
                                                    """
                                            )
                                    }
                            )
                    }
            ))
    @PostMapping("/auth/changeUsername")
    public AjaxResult changeUsername(HttpServletRequest request, @Validated @RequestBody ChangeUsername params) {
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
        UserDetails userDetails = userService.loadUserByUsername(request.getRemoteUser());

        // 验证密码
        PasswordEncoder encoder = SpringUtils.getBean(PasswordEncoder.class);
        if (!encoder.matches(params.getPassword(), userDetails.getPassword())) {
            return error(I18nUtils.ControllerRestPassWordOldError());
        }
        // 检查新用户名是否已存在
        if (userDetails.getUsername().equals(params.getNewUsername())) {
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

        return success(I18nUtils.AuthSuccess("EditUserName"));
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