package com.nyx.bot.modules.bot.controller.admin;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config/bot/admin")
public class BotAdminController extends BaseController {

    @Resource
    BotAdminRepository botAdminRepository;


    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody BotAdmin ba) {
        return getDataTable(botAdminRepository.findAllByBotUid(ba.getBotUid(), PageRequest.of(ba.getCurrent() - 1, ba.getSize())));
    }

    private List<Map<String, String>> getPe() {
        return Arrays.stream(PermissionsEnums.values())
                .filter(enums -> enums != PermissionsEnums.MANAGE && enums != PermissionsEnums.OTHER)
                .map(e -> Map.of("label", e.getStr(), "value", e.name()))
                .collect(Collectors.toList());
    }

    @GetMapping("/permissions")
    public AjaxResult getPermissions() {
        return success().put("data", getPe());
    }

    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody BotAdmin ba) {
        if (ba.isValidatePermissions()) {
            return error(I18nUtils.PermissionsBan());
        }
        Optional<BotAdmin> byPermissions = botAdminRepository.findByPermissions(ba.getPermissions());
        if (byPermissions.isPresent()) {
            if (byPermissions.get().getPermissions().equals(PermissionsEnums.SUPER_ADMIN) && ba.getBotUid().equals(byPermissions.get().getBotUid())) {
                return error(I18nUtils.PermissionsOne());
            }
        }
        botAdminRepository.save(ba);
        return success();
    }

    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        botAdminRepository.deleteById(id);
        return success();
    }
}
