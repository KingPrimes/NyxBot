package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.service.BotsService;
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
