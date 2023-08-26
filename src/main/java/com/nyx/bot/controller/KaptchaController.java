package com.nyx.bot.controller;

import com.google.code.kaptcha.Producer;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Slf4j
@Controller
@RequestMapping("/captcha")
public class KaptchaController {

    @Autowired
    Producer captcha;

    @GetMapping("/image")
    public void getCaptChaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        String capText = captcha.createText();

        log.info("验证码:{}",capText);

        request.getSession().setAttribute("captcha",capText);
        BufferedImage image = captcha.createImage(capText);
        ServletOutputStream out = response.getOutputStream();

        ImageIO.write(image,"jpg",out);
        out.flush();
    }

}
