package com.nyx.bot.controller.system;

import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.utils.SpringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    //首页
    @GetMapping({"/", "/index"})
    public String indexX(ModelMap map) {
        map.put("isBW", SpringUtils.getBean(HandOff.class).isBW());
        return "index";
    }

    // 切换主题
    @GetMapping("/system/switchSkin")
    public String switchSkin() {
        return "skin";
    }

    @GetMapping("/system/main")
    public String indexMain() {
        return "main";
    }

    @GetMapping("/password")
    public String password() {
        return "password";
    }


}
