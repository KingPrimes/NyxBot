package com.nyx.bot.controller.api.html.warframe;

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
 * 奸商
 */
@Controller
@RequestMapping("/private")
public class VoidHtmlController {

    @Resource
    TranslationService trans;

    @GetMapping("/getVoidHtml")
    public String getHtml(Model model) {
        GlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.VoidTrader v = sgs.getVoidTrader();
        v.setLocation(v.getLocation().replace(
                StringUtils.quStr(v.getLocation()),
                trans.enToZh(StringUtils.quStr(v.getLocation()))
        ));
        for (GlobalStates.VoidTrader.Inventory inventory : v.getInventory()) {
            inventory.setItem(trans.enToZh(inventory.getItem()).replace("&", "&amp;"));
            inventory.setCredits(inventory.getCredits() / 1000);
        }
        v.setEndString(DateUtils.getDiff(v.getExpiry(), new Date(), true));
        v.setStartString(DateUtils.getDiff(v.getActivation(), new Date(), true));
        model.addAttribute("vo", v);
        return "html/voidTrader";
    }
}
