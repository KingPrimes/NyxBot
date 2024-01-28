package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * 仲裁
 */
@Controller
@RequestMapping("/private")
public class ArbitrationHtmlController {

    @Autowired
    TranslationService trans;

    @GetMapping("/getArbitrationHtml")
    public String getHtml(Model model) {
        GlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.Arbitration arbitration = sgs.getArbitration();
        arbitration.setNode(arbitration.getNode().
                replace(
                        StringUtils.quStr(arbitration.getNode()),
                        trans.enToZh(StringUtils.quStr(arbitration.getNode())
                        )
                ));
        arbitration.setType(trans.enToZh(arbitration.getType()));
        arbitration.setEtc(DateUtils.getDiff((arbitration.getExpiry()), new Date(), true));
        model.addAttribute("arbit", arbitration);
        return "html/arbitration";
    }
}
