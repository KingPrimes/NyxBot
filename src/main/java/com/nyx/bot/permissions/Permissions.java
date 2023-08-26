package com.nyx.bot.permissions;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.entity.BotAdmin;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.SpringUtils;

import java.util.Objects;

public class Permissions {


    /**
     * 判断用户是否是管理员
     *
     * @param bot   Bot
     * @param event 所有消息类型
     * @return 是否是管理员
     */
    public static boolean checkPermissions(Bot bot, AnyMessageEvent event) {
        BotAdmin byAdminUid = SpringUtils.getBean(BotAdminRepository.class).findByAdminUid(event.getUserId());
        if(byAdminUid==null){
            return false;
        }
        PermissionsEnums permissions = byAdminUid.getPermissions();
        return Objects.requireNonNull(permissions) == PermissionsEnums.ADMIN || isAdmin(bot, event);
    }

    /**
     * 判断用户是否是超级管理员
     * @param userId 用户ID
     */
    public static boolean checkSuperAdmin(Long userId) {
        BotAdmin byAdminUid = SpringUtils.getBean(BotAdminRepository.class).findByAdminUid(userId);
        if(byAdminUid==null){
            return false;
        }
        PermissionsEnums permissions = byAdminUid.getPermissions();
        switch (permissions){
            case SUPER_ADMIN,MANAGE -> {
                return true;
            }
        }
       return false;
    }

    /**
     * 检查用户权限
     * @return PermissionsEnums
     */
    public static PermissionsEnums checkAdmin(Bot bot, AnyMessageEvent event){
        if(isAdmin(bot, event)){
            return PermissionsEnums.ADMIN;
        }
        BotAdmin byAdminUid = SpringUtils.getBean(BotAdminRepository.class).findByAdminUid(event.getUserId());
        if(byAdminUid==null){
            return PermissionsEnums.USER;
        }
        return byAdminUid.getPermissions();
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
}
