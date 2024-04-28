package com.nyx.bot.controller.system;

import com.nyx.bot.controller.config.bot.HandOff;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    //首页
    @GetMapping({"/", "/index"})
    public String indexX(Model model) {
        model.addAttribute("isBW", HandOff.isBW());
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
