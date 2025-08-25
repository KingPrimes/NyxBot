package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.res.Arbitration;
import com.nyx.bot.utils.DateUtils;
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
    @GetMapping("/getArbitrationHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        Arbitration arbitration = ArbitrationCache.getArbitration().orElseThrow(() -> new DataNotInfoException("未获取到仲裁信息"));
        arbitration.setEnemy(arbitration.getEnemy().replace("Infestation", "Infested"));
        arbitration.setEtc(DateUtils.getDiff((arbitration.getExpiry()), new Date(), true));
        model.addAttribute("arbit", arbitration);
        return "html/arbitration";
    }

    @GetMapping("/getArbitrationEx")
    public String getArbitrationExHtml(Model model) {
        model.addAttribute("arbitrations", ArbitrationCache.getArbitrationList());
        return "html/arbitration_ex";
    }
}
