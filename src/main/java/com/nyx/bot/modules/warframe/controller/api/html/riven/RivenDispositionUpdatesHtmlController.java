package com.nyx.bot.modules.warframe.controller.api.html.riven;

import com.nyx.bot.modules.warframe.repo.RivenTrendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 紫卡倾向更新列表
 */
@Controller
@RequestMapping("/private")
public class RivenDispositionUpdatesHtmlController {

    RivenTrendRepository repository;

    @Autowired
    void RivenAnalyseTrendHtmlController(RivenTrendRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/getRivenDispositionUpdatesHtml")
    public String getHtml(Model map) {
        map.addAttribute("data", repository.findRivenDisUpdate());
        return "html/rivenDispositionUpdates";
    }

}
