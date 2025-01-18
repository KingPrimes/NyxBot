package com.nyx.bot.controller.system;

import com.nyx.bot.annotation.InternationalizedNotEmpty;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.repo.sys.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
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
        @InternationalizedNotEmpty(message = "controller.rest.password.old.not.empty")
        private String oldPassword;
        @InternationalizedNotEmpty(message = "controller.rest.password.new.not.empty")
        @Min(value = 6, message = "controller.rest.password.length")
        private String newPassword;
        @InternationalizedNotEmpty(message = "controller.rest.password.confirm.not.empty")
        private String confirmPassword;

        public boolean isValid() {
            return newPassword.equals(confirmPassword);
        }

        public boolean isValidOld() {
            return oldPassword.equals(newPassword);
        }

    }

}
