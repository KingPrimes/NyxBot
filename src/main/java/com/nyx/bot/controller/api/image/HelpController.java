package com.nyx.bot.controller.api.image;

import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.core.Constants;
import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.utils.HtmlToImage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/api")
public class HelpController {
    @LogInfo(title = "Help|帮助",codes = Codes.HELP,businessType = BusinessType.IMAGE)
    @GetMapping(value = "/help/{bot}/{user}/{group}/{rawMsg}/{time}",produces = MediaType.IMAGE_PNG_VALUE)
    public void getImage(HttpServletResponse response,@PathVariable long bot, @PathVariable long user, @PathVariable long group, @PathVariable String rawMsg,@PathVariable long time) throws IOException {
        response.setHeader("content-type","image/png");
        response.getOutputStream().write(
                HtmlToImage.conver(
                        Constants.LOCALHOST+"private/getHelpHtml"
                ).toByteArray()
        );
    }


}
