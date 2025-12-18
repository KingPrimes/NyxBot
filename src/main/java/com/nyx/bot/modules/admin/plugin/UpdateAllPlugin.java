package com.nyx.bot.modules.admin.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.admin.utils.UpdateUtils;
import com.nyx.bot.permissions.Permissions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Shiro
@Component
@Slf4j
public class UpdateAllPlugin {

    private final UpdateUtils updateUtils;
    private final String msg = "权限不足！仅超级管理员权限在私聊中可用。";

    public UpdateAllPlugin(UpdateUtils updateUtils) {
        this.updateUtils = updateUtils;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RES_MARKET_ITEMS_CMD, at = AtEnum.BOTH)
    public void updateWarframeResMarketItemsHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            updateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RES_MARKET_ITEMS);
        } else {
            log.info("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RES_MARKET_ITEMS_CMD);
            bot.sendMsg(event, msg, false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RES_MARKET_RIVEN_CMD, at = AtEnum.BOTH)
    public void updateWarframeResMarketRivenHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            updateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RES_MARKET_RIVEN);
        } else {
            log.info("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RES_MARKET_RIVEN_CMD);
            bot.sendMsg(event, msg, false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_SISTER_CMD, at = AtEnum.BOTH)
    public void updateWarframeSisterHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            updateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_SISTER);
        } else {
            log.info("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_SISTER_CMD);
            bot.sendMsg(event, msg, false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_TAR_CMD, at = AtEnum.BOTH)
    public void updateWarframeTarHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            updateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_TAR);
        } else {
            log.info("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_TAR_CMD);
            bot.sendMsg(event, msg, false);
        }
    }

}
