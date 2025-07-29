package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 双衍王境
 */
@Controller
@RequestMapping("/private")
@Slf4j
public class DuviriCycleController {


    @Resource
    TranslationService trans;

    @GetMapping("/getDuviriCycleHtml")
    public String getHtml(Model model) throws DataNotInfoException {
//        GlobalStates gs = CacheUtils.getGlobalState();
//        List<String> normal = new ArrayList<>();
//        List<String> hard = new ArrayList<>();
//        GlobalStates.DuviriCycle duviriCycle = gs.getDuviriCycle();
//        for (GlobalStates.DuviriCycle.Choices choice : duviriCycle.getChoices()) {
//            if (choice.getCategory().equals("normal")) {
//                normal = choice.getChoices();
//            }
//            if (choice.getCategory().equals("hard")) {
//                hard = choice.getChoices().stream().map(s -> trans.enToZh(StringUtils.addSpaceBetweenWords(s))).toList();
//            }
//        }
//        log.info("normal:{}, hard:{}", normal, hard);
//        model.addAttribute("normal", normal);
//        model.addAttribute("hard", hard);

        return "html/duviriCycle";
    }

}
