package com.nyx.bot.controller.system;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysUser;
import com.nyx.bot.repo.sys.SysUserRepository;
import com.nyx.bot.service.UserService;
import com.nyx.bot.utils.I18nUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResetPasswordController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    SysUserRepository repository;

    @GetMapping("/resetPwd")
    public String resetPwd() {
        return "password";
    }

    @PostMapping("/password")
    @ResponseBody
    public AjaxResult restPwd(HttpServletRequest request, String oldPassword, String newPassword) {
        UserDetails userDetails = userService.loadUserByUsername(request.getRemoteUser());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.old.error"));
        }
        if (encoder.matches(newPassword, userDetails.getPassword())) {
            return AjaxResult.error(I18nUtils.message("controller.rest.password.o.n"));
        }
        SysUser sysUser = repository.findSysUsersByUserName(userDetails.getUsername());
        sysUser.setPassword(encoder.encode(newPassword));
        repository.save(sysUser);
        return AjaxResult.success();

    }


}
