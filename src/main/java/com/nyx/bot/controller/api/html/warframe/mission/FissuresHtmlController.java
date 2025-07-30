package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.plugin.warframe.utils.FissuresUtils;
import com.nyx.bot.res.worldstate.ActiveMission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/private")
public class FissuresHtmlController {


    @GetMapping("/getFissuresHtml/{type}")
    public String getHtml(Model model, @PathVariable Integer type) throws DataNotInfoException {
        List<ActiveMission> list = FissuresUtils.getFissures(type);
        model.addAttribute("type", type);
        model.addAttribute("fissues", list);
        return "html/fissues";
    }

    @PostMapping("/postSubscribeFissuresHtml")
    public String postSubscribeFissuresHtml(Model model, @RequestBody List<ActiveMission> list) {
        model.addAttribute("fissues", list);
        return "html/subscribeFissues";
    }


}
