package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.res.enums.SyndicateEnum;
import com.nyx.bot.modules.warframe.res.worldstate.SyndicateMission;
import com.nyx.bot.modules.warframe.utils.SyndicateMissionsUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class SyndicateMissionsHtmlController {

    @PostMapping("/postSyndicateMissions")
    public String getHtml(@RequestBody String data, Model model) throws DataNotInfoException {
        SyndicateMission syndicateMissions = SyndicateMissionsUtils.getSyndicateMissions(WarframeCache.getWarframeStatus().getSyndicateMissions(), SyndicateEnum.valueOf(data));
        model.addAttribute("sm", syndicateMissions);
        return "html/syndicateMissions";
    }

}
