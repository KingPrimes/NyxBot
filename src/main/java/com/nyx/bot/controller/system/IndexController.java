package com.nyx.bot.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {


    //首页
    @GetMapping("/")
    public String indexX() {
        return "index";
    }

    @GetMapping("/index")
    public String index() {
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
