package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.utils.SpringUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class WarframeSubscribeCheck {

    /**
     * 获取订阅枚举属性，枚举变Map
     *
     * @return Map<Integer, String>
     */
    public static Map<Integer, String> getSubscribeEnums() {
        return Arrays.stream(SubscribeEnums.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, SubscribeEnums::getNAME));
    }

    /**
     * 获取任务类型枚举属性，枚举变Map
     *
     * @return Map<Integer, String>
     */
    public static Map<Integer, String> getSubscribeMissionTypeEnums() {
        return Arrays.stream(WarframeMissionTypeEnum.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, WarframeMissionTypeEnum::get));
    }

    /**
     * 参数解析增强方法（支持通配符）
     */
    private static SubscribeParams parseSubscribeParams(String raw) {
        String[] parts = raw.split("-");
        SubscribeParams params = new SubscribeParams();

        try {
            // 必填参数：订阅类型
            if (parts.length < 1) throw new IllegalArgumentException();
            params.type = parseSubscribeType(parts[0]);

            // 可选参数处理
            params.missionType = (parts.length > 1 && !parts[1].isEmpty()) ?
                    parseMissionType(parts[1]) : null;

            params.tier = (parts.length > 2 && !parts[2].isEmpty()) ?
                    Integer.parseInt(parts[2]) : null;

            // 校验遗物等级范围
            if (params.tier != null && (params.tier < 1 || params.tier > 5)) {
                throw new IllegalArgumentException("遗物等级需在1-5之间");
            }
        } catch (Exception e) {
            params.error = """
                    参数错误，格式：订阅类型[必填]-任务类型[可选]-遗物等级[可选]
                    示例：
                    订阅9     → 订阅所有裂隙
                    订阅9-2   → 订阅所有捕获裂隙
                    订阅9-2-4 → 订阅后纪捕获裂隙
                    """;
        }
        return params;
    }

    /**
     * 构建订阅信息说明
     */
    private static String buildSubscriptionInfo(SubscribeParams params) {
        StringBuilder sb = new StringBuilder()
                .append("类型: ").append(params.type.getNAME());

        if (params.missionType != null) {
            sb.append("\n任务: ").append(params.missionType.get());
        } else {
            sb.append("\n任务: 全部");
        }

        if (params.tier != null) {
            sb.append("\n等级: ").append(params.tier);
        } else if (params.type == SubscribeEnums.FISSURES) {
            sb.append("\n等级: 全部");
        }

        return sb.toString();
    }

    // 添加参数解析校验方法
    private static SubscribeEnums parseSubscribeType(String input) {
        try {
            int code = Integer.parseInt(input);
            if (code <= 0 || code >= SubscribeEnums.values().length) {
                return SubscribeEnums.ERROR;
            }
            return SubscribeEnums.values()[code];
        } catch (NumberFormatException e) {
            return SubscribeEnums.ERROR;
        }
    }

    private static WarframeMissionTypeEnum parseMissionType(String input) {
        try {
            int code = Integer.parseInt(input);
            if (code <= 0 || code >= WarframeMissionTypeEnum.values().length) {
                return WarframeMissionTypeEnum.ERROR;
            }
            return WarframeMissionTypeEnum.values()[code];
        } catch (NumberFormatException e) {
            return WarframeMissionTypeEnum.ERROR;
        }
    }

    private static MissionSubscribe createNewSubscription(Long botUid, Long subGroup, String groupName) {
        MissionSubscribe sub = new MissionSubscribe();
        sub.setSubGroup(subGroup);
        sub.setGroupName(groupName);
        sub.setSubBotUid(botUid);
        sub.setUsers(new HashSet<>());
        return sub;
    }

    private static void processSubscription(SubscribeParams params, Long userUid,
                                            String userName, MissionSubscribe sub) {

        MissionSubscribeUser user = sub.getUsers().stream()
                .filter(u -> u.getUserId().equals(userUid))
                .findFirst()
                .orElseGet(() -> createNewUser(userUid, userName, sub));

        // 检查是否已存在相同订阅规则
        boolean exists = user.getCheckTypes().stream()
                .anyMatch(t -> t.matches(params.type, params.missionType, params.tier));

        if (!exists) {
            MissionSubscribeUserCheckType checkType = new MissionSubscribeUserCheckType();
            checkType.setSubscribe(params.type);
            checkType.setMissionTypeEnum(params.missionType == null ? WarframeMissionTypeEnum.ERROR : params.missionType); // 允许为null
            checkType.setTierNum(params.tier == null ? 0 : params.tier); // 允许为null
            checkType.setSubscribeUser(user);
            user.getCheckTypes().add(checkType);
        }
    }

    private static MissionSubscribeUser createNewUser(Long userUid, String userName, MissionSubscribe sub) {
        MissionSubscribeUser user = new MissionSubscribeUser();
        user.setUserId(userUid);
        user.setUserName(userName);
        user.setMissionSubscribe(sub);  // 关键关联设置
        user.setCheckTypes(new HashSet<>());
        sub.getUsers().add(user);
        return user;
    }

    /**
     * 订阅
     *
     * @param botUid    机器人ID
     * @param userUid   用户ID
     * @param userName  用户昵称
     * @param subGroup  订阅群组
     * @param groupName 群组昵称
     * @param raw       源消息
     * @return 发送的信息
     */
    @Transactional
    public String userSubscriptions(Long botUid, Long userUid, String userName,
                                    Long subGroup, String groupName, String raw) {
        try {
            // 参数解析
            SubscribeParams params = parseSubscribeParams(raw);
            if (params.hasError()) {
                return params.getErrorMessage();
            }

            MissionSubscribeRepository repo = SpringUtils.getBean(MissionSubscribeRepository.class);
            MissionSubscribe subscription = repo.findBySubGroup(subGroup)
                    .orElseGet(() -> createNewSubscription(botUid, subGroup, groupName));

            processSubscription(params, userUid, userName, subscription);
            repo.save(subscription);

            return "订阅成功！\n" + buildSubscriptionInfo(params);
        } catch (Exception e) {
            log.error("订阅处理失败", e);
            return "订阅失败，请检查参数格式";
        }
    }

    /**
     * 增强版取消订阅方法
     *
     * @param userUid  用户ID
     * @param subGroup 订阅群组
     * @param raw      输入内容 例如："取消订阅3" 或 "取消订阅3-2-5"
     * @return 操作结果
     */
    @Transactional
    public String userCancelSubscribe(Long userUid, Long subGroup, String raw) {
        StringBuffer motion = new StringBuffer();
        MissionSubscribeRepository bean = SpringUtils.getBean(MissionSubscribeRepository.class);
        Optional<MissionSubscribe> missionSubscribe = bean.findBySubGroup(subGroup);

        // 参数解析增强
        String[] split = raw.split("-");
        if (split.length < 1 || split[0].isEmpty()) {
            return """
                    参数错误，格式：订阅类型[必填]-任务类型[可选]-遗物等级[可选]
                    示例：
                    取消订阅9     → 取消订阅所有裂隙
                    取消订阅9-2   → 取消订阅所有捕获裂隙
                    取消订阅9-2-4 → 取消订阅后纪捕获裂隙
                    """;
        }

        // 解析订阅类型
        SubscribeEnums subscribeEnums;
        try {
            int typeCode = Integer.parseInt(split[0]);
            if (typeCode <= 0 || typeCode >= SubscribeEnums.values().length) {
                return "无效的订阅类型代码";
            }
            subscribeEnums = SubscribeEnums.values()[typeCode];
        } catch (NumberFormatException e) {
            return "订阅类型必须为数字";
        }

        // 解析可选参数
        WarframeMissionTypeEnum missionTypeEnum = WarframeMissionTypeEnum.ERROR;
        int tierNum = 0;
        try {
            if (split.length >= 2 && !split[1].isEmpty()) {
                int missionCode = Integer.parseInt(split[1]);
                if (missionCode >= 0 && missionCode < WarframeMissionTypeEnum.values().length) {
                    missionTypeEnum = WarframeMissionTypeEnum.values()[missionCode];
                }
            }
            if (split.length >= 3 && !split[2].isEmpty()) {
                tierNum = Integer.parseInt(split[2]);
            }
        } catch (NumberFormatException e) {
            return "参数包含非法数字";
        }
        Integer finalTierNum = tierNum;
        WarframeMissionTypeEnum finalMissionTypeEnum = missionTypeEnum;
        missionSubscribe.ifPresentOrElse(m -> {
            Set<MissionSubscribeUser> subUsers = m.getUsers();
            int removedCount = 0;
            List<MissionSubscribeUserCheckType> toRemove;
            // 使用迭代器安全删除
            for (MissionSubscribeUser user : subUsers) {
                if (!user.getUserId().equals(userUid)) continue;

                // 过滤需要删除的检查项
                toRemove = user.getCheckTypes().stream()
                        .filter(t -> t.getSubscribe() == subscribeEnums)
                        .filter(t -> t.getMissionTypeEnum() == null || t.getMissionTypeEnum() == finalMissionTypeEnum)
                        .filter(t -> t.getTierNum() == null || Objects.equals(t.getTierNum(), finalTierNum))
                        .toList();
                List<Long> ids = toRemove.stream().map(MissionSubscribeUserCheckType::getId).toList();
                // 执行删除
                if (!ids.isEmpty()) {
                    SpringUtils.getBean(MissionSubscribeUserCheckTypeRepository.class)
                            .deleteAllByIdInBatch(ids);
                    user.getCheckTypes().removeIf(t -> ids.contains(t.getId()));
                    removedCount = ids.size();
                }

                // 如果用户没有其他订阅项，删除用户
                if (user.getCheckTypes().isEmpty()) {
                    m.getUsers().remove(user);
                    if (m.getUsers().isEmpty()) {
                        bean.delete(m);
                        motion.append("已移除空订阅群组\n");
                    }
                }
            }

            motion.append("成功取消").append(removedCount).append("项订阅");

        }, () -> motion.append("未找到相关订阅"));

        return motion.toString();
    }

    // 参数包装类
    private static class SubscribeParams {
        SubscribeEnums type;
        WarframeMissionTypeEnum missionType;
        Integer tier;
        String error;

        boolean hasError() {
            return error != null || type == null || type == SubscribeEnums.ERROR;
        }

        String getErrorMessage() {
            return error != null ? error : "无效的订阅类型";
        }
    }

}
