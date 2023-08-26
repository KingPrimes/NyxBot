package com.nyx.bot.controller.api.html;

import com.nyx.bot.enums.Codes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/private")
public class PrivateHelpHtmlController {

    @GetMapping("/getHelpHtml")
    public String getHtml(Model model){
        Map<String,String> v = new HashMap<>();
        for (Codes value : Codes.values()) {
            v.put(value.getStr(),value.getPermissions().getStr());
        }
        model.addAttribute("codes",v);
        return "html/help";
    }
}
