package com.nyx.bot.modules.system.controller;

import com.nyx.bot.annotation.NotEmpty;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import com.nyx.bot.modules.system.repo.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
import io.swagger.annotations.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResetPasswordController extends BaseController {

    @Resource
    UserService userService;

    @Resource
    SysUserRepository repository;

    @ApiOperation("重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "params", value = "重置密码参数", dataType = "ResetPassword", paramType = "body", examples = @Example(value = {
                    @ExampleProperty(mediaType = "oldPassword", value = "123456"),
                    @ExampleProperty(mediaType = "newPassword", value = "123456"),
                    @ExampleProperty(mediaType = "confirmPassword", value = "123456"),
            }))
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功"),
            @ApiResponse(code = 400, message = "请求参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/auth/restorePassword")
    public AjaxResult restPwd(HttpServletRequest request, @Validated @RequestBody ResetPassword params) {
        if (params.isValidOld()) return error(I18nUtils.ControllerRestPassWordON());
        if (!params.isValid()) return error(I18nUtils.ControllerRestPassWordONError());


        UserDetails userDetails = userService.loadUserByUsername(request.getRemoteUser());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(params.getOldPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.ControllerRestPassWordOldError());
        }
        if (encoder.matches(params.getNewPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.ControllerRestPassWordON());
        }
        SysUser sysUser = repository.findSysUsersByUserName(userDetails.getUsername());
        sysUser.setPassword(encoder.encode(params.getNewPassword()));
        repository.save(sysUser);
        return AjaxResult.success();
    }


    @Data
    public static class ResetPassword {
        @NotEmpty(message = "controller.rest.password.old.not.empty")
        private String oldPassword;
        @NotEmpty(message = "controller.rest.password.new.not.empty")
        @Min(value = 6, message = "controller.rest.password.length")
        private String newPassword;
        @NotEmpty(message = "controller.rest.password.confirm.not.empty")
        private String confirmPassword;

        public boolean isValid() {
            return newPassword.equals(confirmPassword);
        }

        public boolean isValidOld() {
            return oldPassword.equals(newPassword);
        }

    }

}
