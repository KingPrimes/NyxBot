package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.res.worldstate.SteelPathOffering;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 钢铁
 */
@Controller
@RequestMapping("/private")
public class SteelPathHtmlController {

    @GetMapping("/getSteelPathHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        SteelPathOffering steelPath = WarframeCache.getWarframeStatus().getSteelPath();
        model.addAttribute("stee", steelPath);
        return "html/steePath";
    }
}
