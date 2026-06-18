package com.nyx.bot.controller.auth;


import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.SecurityUtils;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.system.entity.SysUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 获取用户信息
 */
@RestController
@CrossOrigin
public class UserInfoController extends BaseController {
    @GetMapping("/auth/info")
    public ApiResponse<?> getInfo() {
        SysUser loginUser = SecurityUtils.getLoginUser();
        return success(Map.of("userName", loginUser.getUserName()));
    }
}
