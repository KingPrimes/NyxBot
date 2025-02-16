package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
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

    @Resource
    TranslationService trans;

    @GetMapping("/getArbitrationHtml")
    public String getHtml(Model model) throws DataNotInfoException {
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

    @GetMapping("/getArbitrationEx")
    public String getArbitrationExHtml(Model model) {
        //model.addAttribute("arbitrations", CacheUtils.getArbitrationList().stream().peek(a -> a.setEtc(DateUtils.getDiff((a.getExpiry()), new Date(), true))).toList());
        return "html/arbitration_ex";
    }
}
