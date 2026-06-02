package com.nyx.bot.modules.bot.controller.bot;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.modules.bot.service.BotsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bot
 */
@RestController
@RequestMapping("/config/bot")
public class BotController extends BaseController {

    private final BotsService botsService;

    public BotController(BotsService botsService) {
        this.botsService = botsService;
    }

    @GetMapping("/bots")
    public ApiResponse<?> getBots() {
        return botsService.getBots();
    }

    @GetMapping("/friend/{botUid}")
    public ApiResponse<?> getFriendList(@PathVariable Long botUid) {
        return botsService.getFriendList(botUid);
    }

    @GetMapping("/group/{botUid}")
    public ApiResponse<?> getGroupList(@PathVariable Long botUid) {
        return botsService.getGroupList(botUid);
    }


}
