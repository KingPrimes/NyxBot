package com.nyx.bot.controller.auth;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.HttpMethod;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
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
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重置密码
 */
@SecurityScheme(
        name = "Bearer",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        paramName = "Authorization",
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT"
)
@Tag(name = "auth.password", description = "重置密码")
@SecurityRequirement(name = "Bearer")
@RestController
public class ResetPasswordController extends BaseController {

    @Resource
    UserService userService;

    @Resource
    SysUserRepository repository;

    @Operation(
            summary = "重置密码",
            description = "重置密码",
            method = HttpMethod.POST,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "重置密码参数",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResetPassword.class),
                                    examples = {
                                            @ExampleObject(value = """
                                                    {
                                                        "oldPassword": "123456",
                                                        "newPassword": "123456",
                                                        "confirmPassword": "123456"
                                                    }
                                                    """
                                            )
                                    }
                            )
                    }
            ))
    @PostMapping("/auth/restorePassword")
    public AjaxResult restPwd(Authentication authentication, @Validated @RequestBody ResetPassword params) {
        if (params.isValidOld()) return error(I18nUtils.ControllerRestPassWordON());
        if (!params.isValid()) return error(I18nUtils.ControllerRestPassWordONError());


        UserDetails userDetails = userService.loadUserByUsername(authentication.getName());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(params.getOldPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.ControllerRestPassWordOldError());
        }
        if (encoder.matches(params.getNewPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.ControllerRestPassWordON());
        }
        return repository.findSysUsersByUserName(userDetails.getUsername())
                .map(s -> {
                    s.setPassword(encoder.encode(params.getNewPassword()));
                    repository.save(s);
                    return success();
                }).orElse(error(I18nUtils.ControllerRestPassWordONError()));
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
