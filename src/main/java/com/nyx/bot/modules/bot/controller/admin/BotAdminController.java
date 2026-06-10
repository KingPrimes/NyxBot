package com.nyx.bot.modules.bot.controller.admin;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.I18nUtils;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bot 管理员
 */
@RestController
@RequestMapping("/config/bot/admin")
public class BotAdminController extends BaseController {

    private final BotAdminRepository botAdminRepository;

    public BotAdminController(BotAdminRepository botAdminRepository) {
        this.botAdminRepository = botAdminRepository;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody BotAdmin ba) {
        return getDataTable(botAdminRepository.findAllByBotUid(ba.getBotUid(), PageRequest.of(ba.getCurrent() - 1, ba.getSize())));
    }

    private List<Map<String, String>> getPe() {
        return Arrays.stream(PermissionsEnums.values())
                .filter(enums -> enums != PermissionsEnums.MANAGE && enums != PermissionsEnums.OTHER)
                .map(e -> Map.of("label", e.getStr(), "value", e.name()))
                .collect(Collectors.toList());
    }

    @GetMapping("/permissions")
    public ApiResponse<Object> getPermissions() {
        return success(getPe());
    }

    @PostMapping("/save")
    public ApiResponse<Void> save(@Validated @RequestBody BotAdmin ba) {
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
    public ApiResponse<Void> remove(@NonNull @PathVariable("id") Long id) {
        botAdminRepository.deleteById(id);
        return success();
    }
}
