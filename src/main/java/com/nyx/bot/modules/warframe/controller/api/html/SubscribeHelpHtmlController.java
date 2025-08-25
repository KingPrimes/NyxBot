package com.nyx.bot.modules.warframe.controller.api.html;

import com.nyx.bot.modules.warframe.utils.WarframeSubscribeCheck;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class SubscribeHelpHtmlController {

    @GetMapping("/getSubscribeHelp")
    public String getHtml(Model model) {
        model.addAttribute("sub", WarframeSubscribeCheck.getSubscribeEnums());
        model.addAttribute("type", WarframeSubscribeCheck.getSubscribeMissionTypeEnums());
        return "html/subscriberHelp";
    }

}
