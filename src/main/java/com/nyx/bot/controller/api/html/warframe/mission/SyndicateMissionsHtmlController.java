package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.enums.SyndicateKeyEnum;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.plugin.warframe.utils.SyndicateMissionsUtils;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
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
        GlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.SyndicateMissions syndicateMissions = SyndicateMissionsUtils.getSyndicateMissions(sgs, SyndicateKeyEnum.valueOf(data));
        model.addAttribute("sm", syndicateMissions);
        return "html/syndicateMissions";
    }

}
