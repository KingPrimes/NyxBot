package com.nyx.bot.controller.system;

import com.nyx.bot.repo.impl.sys.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Autowired
    SysMenuService menu;


    //首页
    @GetMapping("/")
    public String indexX(ModelMap map) {
        map.put("menu", menu.getChildPerms());
        return "index";
    }

    @GetMapping("/index")
    public String index(ModelMap map) {
        map.put("menu", menu.getChildPerms());
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
