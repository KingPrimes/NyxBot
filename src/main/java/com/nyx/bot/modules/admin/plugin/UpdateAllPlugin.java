package com.nyx.bot.modules.admin.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
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
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_HTML_CMD)
    public void updateHtmlHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_HTML);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_HTML_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RES_MARKET_ITEMS_CMD)
    public void updateWarframeResMarketItemsHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RES_MARKET_ITEMS);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RES_MARKET_ITEMS_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RES_MARKET_RIVEN_CMD)
    public void updateWarframeResMarketRivenHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RES_MARKET_RIVEN);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RES_MARKET_RIVEN_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RES_RM_CMD)
    public void updateWarframeResRmHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RES_RM);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RES_RM_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_RIVEN_CHANGES_CMD)
    public void updateWarframeResRivenChangesHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_RIVEN_CHANGES);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_RIVEN_CHANGES_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_SISTER_CMD)
    public void updateWarframeSisterHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_SISTER);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_SISTER_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_WARFRAME_TAR_CMD)
    public void updateWarframeTarHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_WARFRAME_TAR);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_WARFRAME_TAR_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.UPDATE_JAR_CMD)
    public void updateJarHandler(Bot bot, AnyMessageEvent event) {
        if (Permissions.checkAdmin(bot, event) == PermissionsEnums.SUPER_ADMIN) {
            UpdateUtils.updatePlugin(bot, event, Codes.UPDATE_JAR);
        } else {
            log.debug("群：{} 用户:{} 没有权限使用 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.UPDATE_JAR_CMD);
            bot.sendMsg(event, "权限不足！", false);
        }
    }

}
