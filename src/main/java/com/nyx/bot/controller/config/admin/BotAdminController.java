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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
        return getDataTable(botAdminRepository.findAll(PageRequest.of(ba.getCurrent() - 1, ba.getSize())));
    }

    private Map<String, String> getPe() {
        return Arrays.stream(PermissionsEnums.values()).collect(Collectors.toMap(PermissionsEnums::name, PermissionsEnums::getStr));
    }

    @GetMapping("/add")
    public AjaxResult add() {
        AjaxResult ar = AjaxResult.success();
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        ar.put("permissions", getPe());
        ar.put("bots", container.robots.keySet());
        return ar;
    }

    @GetMapping("/friend/{botUid}")
    public AjaxResult getFriendList(@PathVariable Long botUid) {
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        return container.robots.containsKey(botUid)
                ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getFriendList().getData())
                : new AjaxResult(HttpCodeEnum.ERROR, "Bot not found", null);
    }

    @PostMapping("/save")
    public AjaxResult save(@RequestBody BotAdmin ba) {
        if (ba == null) {
            return error("参数错误！");
        }
        switch (ba.getPermissions()) {
            case MANAGE, OTHER -> {
                return error("不可使用此权限！");
            }
        }
        Optional<BotAdmin> byPermissions = botAdminRepository.findByPermissions(ba.getPermissions());
        if (byPermissions.isPresent()) {
            if (byPermissions.get().getPermissions().equals(PermissionsEnums.SUPER_ADMIN)) {
                return error("超级管理员已存在！且只能有一个。");
            }
        }
        botAdminRepository.save(ba);
        return success();
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = AjaxResult.success();
        //获取好友列表
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        container.robots.forEach((aLong, bot) -> ar.put(String.valueOf(aLong), bot.getFriendList().getData()));
        //获取机器人列表
        ar.put("permissions", getPe());
        ar.put("bots", container.robots.keySet());
        botAdminRepository.findById(id).ifPresent(a -> ar.put("botAdmin", a));
        return ar;
    }


}
