package com.nyx.bot.controller;

import org.springframework.data.annotation.Reference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Reference
    AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String login() {
        return "login";
    }








}
