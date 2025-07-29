package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.res.worldstate.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 平原
 */
@Controller
@RequestMapping("/private")
public class AllCycleHtmlController {
    @GetMapping("/getAllCycleHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        WorldState ws = WarframeCache.getWarframeStatus();
        EarthCycle earth = ws.getEarthCycle();
        //地球
        model.addAttribute("earth", earth);
        //夜灵平野
        CetusCycle cetus = ws.getCetusCycle();
        model.addAttribute("cetus", cetus);
        //福尔图娜
        VallisCycle vallis = ws.getVallisCycle();
        model.addAttribute("vallis", vallis);

        //魔胎之境
        CambionCycle cambion = ws.getCambionCycle();
        model.addAttribute("cambion", cambion);

        //扎里曼
        ZarimanCycle zariman = ws.getZarimanCycle();
        model.addAttribute("zariman", zariman);

        return "html/allCycle";
    }

}
