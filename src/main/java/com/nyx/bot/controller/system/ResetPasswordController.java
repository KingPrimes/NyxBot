package com.nyx.bot.controller.system;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.repo.sys.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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

    @PostMapping("/auth/restorePassword")
    public AjaxResult restPwd(HttpServletRequest request, @Validated @RequestBody ResetPassword params) {
        if (params.isValidOld()) return error("新密码不可与旧密码相同");
        if (!params.isValid()) return error("两次密码输入不一致");


        UserDetails userDetails = userService.loadUserByUsername(request.getRemoteUser());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(params.getOldPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.old.error"));
        }
        if (encoder.matches(params.getNewPassword(), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.o.n"));
        }
        SysUser sysUser = repository.findSysUsersByUserName(userDetails.getUsername());
        sysUser.setPassword(encoder.encode(params.getNewPassword()));
        repository.save(sysUser);
        return AjaxResult.success();
    }


    @Data
    public static class ResetPassword {
        @NotEmpty(message = "旧密码不可为空")
        private String oldPassword;
        @NotEmpty(message = "新密码不可为空")
        @Min(value = 6, message = "密码长度不可小于6位")
        private String newPassword;
        @NotEmpty(message = "确认密码不可为空")
        private String confirmPassword;

        public boolean isValid() {
            return newPassword.equals(confirmPassword);
        }

        public boolean isValidOld() {
            return oldPassword.equals(newPassword);
        }

    }

}
