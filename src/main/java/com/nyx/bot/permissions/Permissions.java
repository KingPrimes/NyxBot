package com.nyx.bot.permissions;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.PermissionsEnums;

import java.util.Objects;

public class Permissions {


    /**
     * 判断用户是否是管理员
     *
     * @param bot   Bot
     * @param event 所有消息类型
     * @return 是否是管理员
     */
    public static boolean checkPermissions(Bot bot, AnyMessageEvent event, PermissionsEnums ps) {
        return isAdmin(bot, event) || PermissionsEnums.ADMIN.equals(ps) || PermissionsEnums.SUPER_ADMIN.equals(ps);
    }

    /**
     * 判断用户是否是超级管理员
     * @param userId 用户ID
     * @param ps 权限
     */
    public static boolean checkSuperAdmin(Long userId, PermissionsEnums ps) {
        return PermissionsEnums.SUPER_ADMIN.equals(ps);
    }


    /**
     * 判断用户是否是管理员或者群主 或系统管理员
     *
     * @param bot   bot
     * @param event event
     * @return true 是管理员或群主
     */
    private static boolean isAdmin(Bot bot, AnyMessageEvent event) {
        String role;
        try {
            role = bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), true).getData().getRole();
        } catch (Exception e) {
            return false;
        }
        if (role == null || role.isEmpty()) {
            return false;
        }
        return role.equals("owner") || role.equals("admin");
    }

    /**
     * 判断机器人是否是管理员
     *
     * @param bot     bot
     * @param groupId groupId
     * @return true 是管理员或群主
     */
    public static boolean isAdmin(Bot bot, long groupId) {
        String role = Objects.requireNonNull(bot.getGroupMemberInfo(groupId, bot.getSelfId(), true)).getData().getRole();
        if (role == null || role.isEmpty()) {
            return false;
        }
        return role.equals("owner") || role.equals("admin");
    }

}
