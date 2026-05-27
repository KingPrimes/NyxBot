package com.nyx.bot.permissions;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.modules.bot.entity.BotAdmin;
import com.nyx.bot.modules.bot.repo.BotAdminRepository;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.SpringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Permissions {

    private static final String ADMIN_CACHE_KEY_PREFIX = "admin-perm-";

    /**
     * 判断用户是否是管理员
     *
     * @param bot   Bot
     * @param event 所有消息类型
     * @return 是否是管理员
     */
    public static boolean checkPermissions(Bot bot, AnyMessageEvent event) {
        if (isAdmin(bot, event)) {
            return true;
        }
        return checkAdminFromDb(event.getUserId());
    }

    private static boolean checkAdminFromDb(Long userId) {
        String cacheKey = ADMIN_CACHE_KEY_PREFIX + userId;
        Boolean cached = CacheUtils.get(CacheUtils.SYSTEM, cacheKey, Boolean.class);
        if (cached != null) {
            return cached;
        }
        Optional<BotAdmin> admin = SpringUtils.getBean(BotAdminRepository.class).findByAdminUid(userId);
        boolean isAdmin = admin.isPresent()
                && (Objects.requireNonNull(admin.get().getPermissions()) == PermissionsEnums.ADMIN
                    || admin.get().getPermissions() == PermissionsEnums.SUPER_ADMIN);
        CacheUtils.putWithExpiry(CacheUtils.SYSTEM, cacheKey, isAdmin, 5, TimeUnit.MINUTES);
        return isAdmin;
    }

    /**
     * 判断用户是否是超级管理员
     *
     * @param userId 用户ID
     */
    public static boolean checkSuperAdmin(Long userId) {
        String cacheKey = ADMIN_CACHE_KEY_PREFIX + "super-" + userId;
        PermissionsEnums cached = CacheUtils.get(CacheUtils.SYSTEM, cacheKey, PermissionsEnums.class);
        if (cached != null) {
            return cached == PermissionsEnums.SUPER_ADMIN;
        }
        Optional<BotAdmin> admin = SpringUtils.getBean(BotAdminRepository.class).findByAdminUid(userId);
        if (admin.isEmpty()) {
            return false;
        }
        PermissionsEnums permissions = Objects.requireNonNull(admin.get().getPermissions());
        boolean isSuperAdmin = permissions == PermissionsEnums.SUPER_ADMIN;
        CacheUtils.putWithExpiry(CacheUtils.SYSTEM, cacheKey, permissions, 5, TimeUnit.MINUTES);
        return isSuperAdmin;
    }

    /**
     * 检查用户权限
     *
     * @return PermissionsEnums
     */
    public static PermissionsEnums checkAdmin(Bot bot, AnyMessageEvent event) {
        if (isAdmin(bot, event)) {
            return PermissionsEnums.ADMIN;
        }
        if (checkSuperAdmin(event.getUserId())) {
            return PermissionsEnums.SUPER_ADMIN;
        }
        return PermissionsEnums.USER;
    }


    /**
     * 判断用户是否是管理员或者群主
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
