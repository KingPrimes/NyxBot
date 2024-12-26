package com.nyx.bot.controller.system;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.repo.sys.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ResetPasswordController extends BaseController {

    @Resource
    UserService userService;

    @Resource
    SysUserRepository repository;

    @PostMapping("/auth/restorePassword")
    public AjaxResult restPwd(HttpServletRequest request, @RequestBody Map<String, String> params) {
        if (params.isEmpty()) return error("密码不可为空");
        if (params.get("newPassword").length() < 6) return error("密码长度不可小于6位");
        if (params.get("newPassword").equals(params.get("oldPassword"))) return error("新密码不可与旧密码相同");
        if (!params.get("newPassword").equals(params.get("confirmPassword"))) return error("两次密码输入不一致");


        UserDetails userDetails = userService.loadUserByUsername(request.getRemoteUser());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(params.get("oldPassword"), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.old.error"));
        }
        if (encoder.matches(params.get("newPassword"), userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.o.n"));
        }
        SysUser sysUser = repository.findSysUsersByUserName(userDetails.getUsername());
        sysUser.setPassword(encoder.encode(params.get("newPassword")));
        repository.save(sysUser);
        return AjaxResult.success();
    }


}
