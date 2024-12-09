package com.nyx.bot.controller.api.html;

import com.nyx.bot.utils.SystemInfoUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.UnknownHostException;

@Controller
@RequestMapping("/private")
public class SystemInfoHtmlController {

    @GetMapping("/getSystemInfoHtml")
    public String getHtml(Model model) throws UnknownHostException {
        model.addAttribute("info", SystemInfoUtils.getInfo());
        return "html/systemInfo";
    }
}
