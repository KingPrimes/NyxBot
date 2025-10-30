package com.nyx.bot.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "index", description = "首页")
@Controller
public class IndexController {

    //首页
    @GetMapping({"/", "/index", "/login"})
    public String indexX() {
        return "index";
    }
}
