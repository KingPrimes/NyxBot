package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * 每日特惠
 */
@Controller
@RequestMapping("/private")
public class DailyDealsHtmlController {
    @Autowired
    TranslationService trans;

    @GetMapping("/getDailyDealsHtml")
    public String getHtml(Model model) {
        SocketGlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates state = sgs.getPacket().getData();
        GlobalStates.DailyDeals deals = state.getDailyDeals().get(0);
        deals.setItem(trans.enToZh(deals.getItem()));
        deals.setEta(DateUtils.getDiff(deals.getExpiry(), new Date(), true));
        model.addAttribute("deals", deals);
        return "html/daily";
    }
}
