package com.nyx.bot.controller.config;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.config.TokenKeys;
import com.nyx.bot.repo.warframe.TokenKeysRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/token")
public class ConfigTokenKeysController extends BaseController {

    @Resource
    private TokenKeysRepository repository;

    @GetMapping
    public AjaxResult get() {
        TokenKeys tokenKeys = repository.findById(1L).orElse(new TokenKeys());
        if (tokenKeys.getTks() != null && !tokenKeys.getTks().isEmpty()) {
            tokenKeys.setTks("****************");
        }
        return success().put("data", tokenKeys);
    }

    @PostMapping
    public AjaxResult save(@RequestBody TokenKeys tokenKeys) {
        tokenKeys.setId(1L);
        if (tokenKeys.getTks().contains("************")) {
            return error();
        }
        repository.save(tokenKeys);
        return success();
    }
}
