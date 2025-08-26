package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.res.worldstate.ActiveMission;
import com.nyx.bot.modules.warframe.utils.FissuresUtils;
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
