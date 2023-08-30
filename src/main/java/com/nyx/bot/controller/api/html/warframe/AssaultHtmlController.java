package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
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
 * 突击
 */
@Controller
@RequestMapping("/private")
public class AssaultHtmlController {
    @Autowired
    TranslationService trans;

    @GetMapping("/getAssaultHtml")
    public String getHtml(Model model) {
        SocketGlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.Sortie assault = sgs.getPacket().getData().getSortie();
        assault.setEta(DateUtils.getDiff((assault.getExpiry()), new Date(), true));

        for (GlobalStates.Sortie.Variants variant : assault.getVariants()) {
            variant.setNode(variant.getNode()
                    .replace(
                            StringUtils.quStr(
                                    variant.getNode()),
                            trans.enToZh(StringUtils.quStr(variant.getNode()))));
            variant.setMissionType(trans.enToZh(variant.getMissionType()));
            variant.setModifier(trans.enToZh(variant.getModifier()));
            variant.setModifierDescription(trans.enToZh(variant.getModifierDescription()));
        }

        model.addAttribute("assault", assault);
        return "html/assault";
    }
}
