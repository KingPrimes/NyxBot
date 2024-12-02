package com.nyx.bot.controller.config.admin;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.bot.BotAdmin;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/config/bot/admin")
public class BotAdminController extends BaseController {


    String prefix = "config/bot/admin/";

    @Resource
    BotAdminRepository botAdminRepository;

    @GetMapping
    public String loading(Model model) {
        model.addAttribute("pe", getPe());
        return prefix + "index";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(BotAdmin ba) {
        return getDataTable(botAdminRepository.findAll(PageRequest.of(ba.getPageNum() - 1, ba.getPageSize())));
    }

    private Map<String, String> getPe() {
        return Arrays.stream(PermissionsEnums.values()).collect(Collectors.toMap(PermissionsEnums::name, PermissionsEnums::getStr));
    }

    @GetMapping("/add")
    public String add(Model model) {
        //添加权限列表
        model.addAttribute("pe", PermissionsEnums.values());

        BotContainer container = SpringUtils.getBean(BotContainer.class);

        //获取机器人列表
        model.addAttribute("bots", container.robots.keySet());

        return prefix + "add";
    }

    @GetMapping("/friend")
    @ResponseBody
    public AjaxResult getFriendList(Long botUid) {
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        return container.robots.containsKey(botUid)
                ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getFriendList().getData())
                : new AjaxResult(HttpCodeEnum.ERROR, "Bot not found", null);
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(BotAdmin ba) {
        if (ba == null) {
            return error("参数错误！");
        }
        switch (ba.getPermissions()) {
            case MANAGE, OTHER -> {
                return error("不可使用此权限！");
            }
        }
        BotAdmin byAdminUid = botAdminRepository.findByAdminUid(ba.getAdminUid());
        if (byAdminUid != null) {
            return error("已存在！");
        }
        byAdminUid = botAdminRepository.findByPermissions(ba.getPermissions());
        if (byAdminUid != null) {
            if (byAdminUid.getPermissions().equals(PermissionsEnums.SUPER_ADMIN)) {
                return error("超级管理员已存在！且只能有一个。");
            }
        }
        botAdminRepository.save(ba);
        return success();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("pe", PermissionsEnums.values());
        //获取好友列表
        BotContainer container = SpringUtils.getBean(BotContainer.class);
        container.robots.forEach((aLong, bot) -> model.addAttribute("ab", bot.getFriendList().getData()));
        //获取机器人列表
        model.addAttribute("bots", container.robots.keySet());
        botAdminRepository.findById(id).ifPresent(a -> model.addAttribute("ba", a));
        return prefix + "edit";
    }





}
