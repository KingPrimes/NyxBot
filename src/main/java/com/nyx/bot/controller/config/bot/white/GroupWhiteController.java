package com.nyx.bot.controller.config.bot.white;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.NyxConfig;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.bot.white.GroupWhite;
import com.nyx.bot.repo.impl.white.WhiteService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/group")
public class GroupWhiteController extends BaseController {

    @Resource
    WhiteService whiteService;


    @PostMapping("/list")
    public ResponseEntity<?> list(GroupWhite white) {
        return getDataTable(whiteService.list(white));
    }


    @GetMapping("/add")
    public AjaxResult add() {
        AjaxResult ar = AjaxResult.success();
        SpringUtils.getBean(BotContainer.class).robots.forEach((aLong, bot) -> ar.put(String.valueOf(aLong), bot.getGroupList().getData()));
        return ar;
    }

    @PostMapping("/save")
    public AjaxResult add(GroupWhite white) {
        if (white == null) return error();
        return toAjax(whiteService.save(white) != null);
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable("id") Long id) {
        return success().put("white", whiteService.findByGroup(id));
    }


    @PostMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.remove(id);
        return success();
    }

    @PostMapping("/handoff")
    public AjaxResult handoff() {
        NyxConfig nyxConfig = HandOff.getConfig();
        nyxConfig.setIsBlackOrWhite(!nyxConfig.getIsBlackOrWhite());
        return toAjax(HandOff.handoff(nyxConfig));
    }

}
