package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.entity.config.TokenKeys;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.TokenKeysRepository;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.SpringUtils;
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
        if (arbitration != null) {
            arbitration.setNode(arbitration.getNode().
                    replace(
                            StringUtils.quStr(arbitration.getNode()),
                            trans.enToZh(StringUtils.quStr(arbitration.getNode())
                            )
                    ));
            arbitration.setEnemy(arbitration.getEnemy().replace("Infestation","Infested"));
            arbitration.setType(trans.enToZh(arbitration.getType()));
            arbitration.setEtc(DateUtils.getDiff((arbitration.getExpiry()), new Date(), true));
            model.addAttribute("arbit", arbitration);
            return "html/arbitration";
        } else {
            throw new DataNotInfoException("The arbitration data was not obtained, please check whether to enter the key!");
        }

    }

    @GetMapping("/getArbitrationEx")
    public String getArbitrationExHtml(Model model) {
        model.addAttribute("arbitrations", CacheUtils.getArbitrationList(SpringUtils.getBean(TokenKeysRepository.class).findById(1L).orElse(new TokenKeys()).getTks()).stream().filter(ArbitrationPre::isWorth).limit(10).peek(a -> a.setEtc(DateUtils.getDiff((a.getExpiry()), new Date(), true))).toList());
        return "html/arbitration_ex";
    }
}
