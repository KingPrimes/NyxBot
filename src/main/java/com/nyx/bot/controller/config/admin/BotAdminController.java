package com.nyx.bot.controller.config.admin;

import com.fasterxml.jackson.annotation.JsonView;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.BotAdmin;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.SpringUtils;
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

    @GetMapping("/bots")
    public AjaxResult getBots() {
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        if (container.robots.isEmpty()) return error("请链接机器人后操作！\n注意：官方机器人无法获取好友列表。");
        return success().put("data", container.robots.keySet().stream().map(k -> Map.of("label", container.robots.get(k).getLoginInfo().getData().getNickname(), "value", k)).collect(Collectors.toList()));
    }

    @GetMapping("/friend/{botUid}")
    public AjaxResult getFriendList(@PathVariable Long botUid) {
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        return container.robots.containsKey(botUid)
                ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getFriendList().getData().stream().map(f -> Map.of("label", f.getNickname(), "value", f.getUserId())).collect(Collectors.toList()))
                : new AjaxResult(HttpCodeEnum.ERROR, "Bot not found", null);
    }

    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody BotAdmin ba) {
        if (ba.isValidatePermissions()) {
            return error("不可使用此权限！");
        }
        Optional<BotAdmin> byPermissions = botAdminRepository.findByPermissions(ba.getPermissions());
        if (byPermissions.isPresent()) {
            if (byPermissions.get().getPermissions().equals(PermissionsEnums.SUPER_ADMIN) && ba.getBotUid().equals(byPermissions.get().getBotUid())) {
                return error("超级管理员已存在！且只能有一个。");
            }
        }
        botAdminRepository.save(ba);
        return success();
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = AjaxResult.success();
        botAdminRepository.findById(id).ifPresent(a -> ar.put("data", a));
        return ar;
    }
}
