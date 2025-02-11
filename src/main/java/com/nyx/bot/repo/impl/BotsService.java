package com.nyx.bot.repo.impl;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotsService {
    @Resource
    BotContainer container;

    /***
     * 获取机器人列表
     */
    public AjaxResult getBots() {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        return AjaxResult.success().put("data", container.robots.keySet().stream().map(k -> Map.of("label", container.robots.get(k).getLoginInfo().getData().getNickname(), "value", k)).collect(Collectors.toList()));

    }

    /**
     * 获取好友列表
     *
     * @param botUid 机器人UID
     */
    public AjaxResult getFriendList(Long botUid) {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        return container.robots.containsKey(botUid)
                ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getFriendList().getData().stream().map(f -> Map.of("label", f.getNickname(), "value", f.getUserId())).collect(Collectors.toList()))
                : new AjaxResult(HttpCodeEnum.ERROR, "此机器人未链接", null);
    }

    /**
     * 获取群列表
     *
     * @param botUid 机器人UID
     */
    public AjaxResult getGroupList(Long botUid) {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        return container.robots.containsKey(botUid)
                ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getGroupList().getData().stream().map(f -> Map.of("label", f.getGroupName(), "value", f.getGroupId())).collect(Collectors.toList()))
                : new AjaxResult(HttpCodeEnum.ERROR, "此机器人未链接", null);
    }
}
