package com.nyx.bot.controller;

import com.nyx.bot.annotation.LogInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @LogInfo(title = "登录")
    @GetMapping("/login")
    public String login(){
        return "login";
    }


    @GetMapping("/")
    public String index(){
        return "index";
    }




}
