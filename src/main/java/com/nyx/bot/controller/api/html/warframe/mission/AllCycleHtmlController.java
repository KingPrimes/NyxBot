package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.Locale;

/**
 * 平原
 */
@Controller
@RequestMapping("/private")
public class AllCycleHtmlController {

    @Resource
    TranslationService trans;

    @GetMapping("/getAllCycleHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        GlobalStates gs = CacheUtils.getGlobalState();
        //地球
        GlobalStates.EarthCycle earth = gs.getEarthCycle();
        earth.setState(trans.enToZh(earth.getState()));
        earth.setTimeLeft(DateUtils.getDiff((earth.getExpiry()), new Date(), true));
        model.addAttribute("earth", earth);
        //夜灵平野
        GlobalStates.CetusCycle cetus = gs.getCetusCycle();
        cetus.setState(trans.enToZh(cetus.getState()));
        cetus.setTimeLeft(DateUtils.getDiff((cetus.getExpiry()), new Date(), true));
        model.addAttribute("cetus", cetus);
        //福尔图娜
        GlobalStates.VallisCycle vallis = gs.getVallisCycle();
        vallis.setState(trans.enToZh(vallis.getState()));
        vallis.setTimeLeft(DateUtils.getDiff((vallis.getExpiry()), new Date(), true));
        model.addAttribute("vallis", vallis);

        //魔胎之境
        GlobalStates.CambionCycle cambion = gs.getCambionCycle();
        cambion.setDump(cambion.getDump().toUpperCase(Locale.ROOT));
        cambion.setTimeLeft(DateUtils.getDiff((cambion.getExpiry()), new Date(), true));
        model.addAttribute("cambion", cambion);

        //扎里曼
        GlobalStates.ZarimanCycle zariman = gs.getZarimanCycle();
        zariman.setState(zariman.getState().toUpperCase(Locale.ROOT));
        zariman.setTimeLeft(DateUtils.getDiff((zariman.getExpiry()), new Date(), true));
        model.addAttribute("zariman", zariman);

        return "html/allCycle";
    }

}
