package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Slf4j
@Controller
@RequestMapping("/private")
public class ArbitrationExHtmlController {


    @GetMapping("/getArbitrationEx")
    public String getArbitrationExHtml(Model model) {
        model.addAttribute("arbitrations", CacheUtils.getArbitrationList().stream().peek(a -> a.setEtc(DateUtils.getDiff((a.getExpiry()), new Date(), true))).toList());
        return "html/arbitration_ex";
    }


}
