package com.nyx.bot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    //首页
    @GetMapping({"/", "/index", "/login"})
    public String indexX() {
        return "index";
    }
}
