package com.nyx.bot.controller.api.html.warframe.mission;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.ArbitrationPre;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.ResourceUtil;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
}
