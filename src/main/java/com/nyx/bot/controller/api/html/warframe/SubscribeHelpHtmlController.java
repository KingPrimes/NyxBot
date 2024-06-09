package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.plugin.warframe.utils.WarframeSubscribeCheck;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class SubscribeHelpHtmlController {

    @GetMapping("/getSubscribeHelp")
    public String getHtml(Model model) {
        model.addAttribute("sub",WarframeSubscribeCheck.getSubscribeEnums());
        model.addAttribute("type",WarframeSubscribeCheck.getSubscribeMissionTypeEnums());
        return "html/subscriberHelp";
    }

}
