package com.nyx.bot.controller.config.bot;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.repo.impl.BotsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config/bot")
public class BotController extends BaseController {

    @Resource
    BotsService botsService;

    @GetMapping("/bots")
    public AjaxResult getBots() {
        return botsService.getBots();
    }

    @GetMapping("/friend/{botUid}")
    public AjaxResult getFriendList(@PathVariable Long botUid) {
        return botsService.getFriendList(botUid);
    }

    @GetMapping("/group/{botUid}")
    public AjaxResult getGroupList(@PathVariable Long botUid) {
        return botsService.getGroupList(botUid);
    }


}
