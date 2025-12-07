package com.nyx.bot.modules.warframe.application;

import com.nyx.bot.common.exception.ServiceException;
import com.nyx.bot.modules.warframe.domain.valueobject.SubscriptionCommand;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserRepository;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * 订阅应用服务
 * 负责订阅和取消订阅的业务逻辑
 * <p>
 * 核心职责：
 * 1. 处理用户订阅请求
 * 2. 处理取消订阅请求
 * 3. 管理订阅组、用户、规则的关系
 */
@Slf4j
@Service
public class SubscriptionApplicationService {

    private final MissionSubscribeRepository subscribeRepo;
    private final MissionSubscribeUserCheckTypeRepository checkTypeRepo;
    private final MissionSubscribeUserRepository userRepo;

    public SubscriptionApplicationService(
            MissionSubscribeRepository subscribeRepo,
            MissionSubscribeUserCheckTypeRepository checkTypeRepo,
            MissionSubscribeUserRepository userRepo
    ) {
        this.subscribeRepo = subscribeRepo;
        this.checkTypeRepo = checkTypeRepo;
        this.userRepo = userRepo;
        log.info("SubscriptionApplicationService 初始化完成");
    }

    /**
     * 用户订阅
     *
     * @param command 订阅命令
     * @return 订阅结果消息
     */
    @Transactional
    public String subscribe(SubscriptionCommand command) {
        try {
            log.info("处理订阅请求 [group:{}] [user:{}] [type:{}]",
                    command.getGroupId(), command.getUserId(), command.getSubscribeType());

            // 1. 获取或创建订阅组
            MissionSubscribe subscription = subscribeRepo
                    .findBySubGroup(command.getGroupId())
                    .orElseGet(() -> createNewSubscription(command));

            // 2. 获取或创建用户
            MissionSubscribeUser user = subscription.getUsers().stream()
                    .filter(u -> u.getUserId().equals(command.getUserId()))
                    .findFirst()
                    .orElseGet(() -> createNewUser(command, subscription));

            // 3. 检查是否已存在相同规则
            boolean exists = user.getCheckTypes().stream()
                    .anyMatch(rule -> matchesCommand(rule, command));

            if (exists) {
                return "您已经订阅过该内容了！\n" + buildSubscriptionInfo(command);
            }

            // 4. 创建新规则
            MissionSubscribeUserCheckType rule = new MissionSubscribeUserCheckType();
            rule.setSubscribe(command.getSubscribeType());
            rule.setMissionTypeEnum(command.getMissionType());
            rule.setTierNum(command.getTier());
            rule.setSubscribeUser(user);

            user.getCheckTypes().add(rule);

            // 5. 保存
            subscribeRepo.save(subscription);

            log.info("订阅成功 [group:{}] [user:{}] [type:{}] [mission:{}] [tier:{}]",
                    command.getGroupId(),
                    command.getUserId(),
                    command.getSubscribeType().getNAME(),
                    command.getMissionType() != null ? command.getMissionType().getName() : "全部",
                    command.getTier() != null ? command.getTier() : "全部");

            return "订阅成功！\n" + buildSubscriptionInfo(command);

        } catch (Exception e) {
            log.error("订阅失败", e);
            return "订阅失败：" + e.getMessage();
        }
    }

    /**
     * 取消订阅
     *
     * @param groupId       群组ID
     * @param userId        用户ID
     * @param subscribeType 订阅类型
     * @param missionType   任务类型（可选）
     * @param tier          遗物等级（可选）
     * @return 取消结果消息
     */
    @Transactional
    public String unsubscribe(
            Long groupId,
            Long userId,
            SubscribeEnums subscribeType,
            MissionTypeEnum missionType,
            Integer tier
    ) {
        try {
            log.info("处理取消订阅请求 [group:{}] [user:{}] [type:{}]",
                    groupId, userId, subscribeType);

            // 1. 查找订阅组
            MissionSubscribe subscription = subscribeRepo
                    .findBySubGroup(groupId)
                    .orElse(null);

            if (subscription == null) {
                return "未找到相关订阅";
            }

            // 2. 查找用户
            MissionSubscribeUser user = subscription.getUsers().stream()
                    .filter(u -> u.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                return "您还没有任何订阅";
            }

            // 3. 查找并删除匹配的规则
            List<MissionSubscribeUserCheckType> toRemove = user.getCheckTypes().stream()
                    .filter(rule -> matchesUnsubscribeCondition(rule, subscribeType, missionType, tier))
                    .toList();

            if (toRemove.isEmpty()) {
                return "未找到匹配的订阅规则";
            }

            // 4. 删除规则
            List<Long> ruleIds = toRemove.stream()
                    .map(MissionSubscribeUserCheckType::getId)
                    .filter(Objects::nonNull)
                    .toList();

            if (!ruleIds.isEmpty()) {
                checkTypeRepo.deleteAllByIdInBatch(ruleIds);
            }
            toRemove.forEach(user.getCheckTypes()::remove);

            // 5. 清理空用户和空订阅组
            if (user.getCheckTypes().isEmpty()) {
                subscription.getUsers().remove(user);

                if (subscription.getUsers().isEmpty()) {
                    subscribeRepo.delete(subscription);
                    log.info("删除空订阅组 [group:{}]", groupId);
                }
            }

            log.info("取消订阅成功 [group:{}] [user:{}] [count:{}]",
                    groupId, userId, toRemove.size());

            return String.format("成功取消 %d 项订阅", toRemove.size());

        } catch (Exception e) {
            log.error("取消订阅失败", e);
            return "取消订阅失败：" + e.getMessage();
        }
    }

    /**
     * 创建新订阅组
     */
    private MissionSubscribe createNewSubscription(SubscriptionCommand command) {
        MissionSubscribe subscription = new MissionSubscribe();
        subscription.setSubGroup(command.getGroupId());
        subscription.setGroupName(command.getGroupName());
        subscription.setSubBotUid(command.getBotUid());
        subscription.setUsers(new HashSet<>());

        log.info("创建新订阅组 [group:{}] [bot:{}]",
                command.getGroupId(), command.getBotUid());

        return subscription;
    }

    /**
     * 创建新用户
     */
    private MissionSubscribeUser createNewUser(
            SubscriptionCommand command,
            MissionSubscribe subscription
    ) {
        MissionSubscribeUser user = new MissionSubscribeUser();
        user.setUserId(command.getUserId());
        user.setUserName(command.getUserName());
        user.setMissionSubscribe(subscription);
        user.setCheckTypes(new HashSet<>());

        subscription.getUsers().add(user);

        log.info("创建新用户 [user:{}] [name:{}]",
                command.getUserId(), command.getUserName());

        return user;
    }

    /**
     * 判断规则是否匹配命令
     */
    private boolean matchesCommand(
            MissionSubscribeUserCheckType rule,
            SubscriptionCommand command
    ) {
        return rule.getSubscribe() == command.getSubscribeType() &&
                Objects.equals(rule.getMissionTypeEnum(), command.getMissionType()) &&
                Objects.equals(rule.getTierNum(), command.getTier());
    }

    /**
     * 判断规则是否匹配取消订阅条件
     */
    private boolean matchesUnsubscribeCondition(
            MissionSubscribeUserCheckType rule,
            SubscribeEnums subscribeType,
            MissionTypeEnum missionType,
            Integer tier
    ) {
        // 订阅类型必须匹配
        if (rule.getSubscribe() != subscribeType) {
            return false;
        }

        // 任务类型匹配（null 表示匹配全部）
        if (missionType != null && !Objects.equals(rule.getMissionTypeEnum(), missionType)) {
            return false;
        }

        // 遗物等级匹配（null 表示匹配全部）
        if (tier != null && !Objects.equals(rule.getTierNum(), tier)) {
            return false;
        }

        return true;
    }

    /**
     * 构建订阅信息描述
     */
    private String buildSubscriptionInfo(SubscriptionCommand command) {
        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━\n");
        sb.append("类型: ").append(command.getSubscribeType().getNAME());

        if (command.getMissionType() != null) {
            sb.append("\n任务: ").append(command.getMissionType().getName());
        } else {
            sb.append("\n任务: 全部");
        }

        if (command.getTier() != null) {
            sb.append("\n等级: ").append(getTierName(command.getTier()));
        } else if (command.getSubscribeType() == SubscribeEnums.FISSURES) {
            sb.append("\n等级: 全部");
        }

        sb.append("\n━━━━━━━━━━━━━━━━");
        return sb.toString();
    }

    /**
     * 获取遗物等级名称
     */
    private String getTierName(Integer tier) {
        return switch (tier) {
            case 1 -> "古纪 (Lith)";
            case 2 -> "前纪 (Meso)";
            case 3 -> "中纪 (Neo)";
            case 4 -> "后纪 (Axi)";
            case 5 -> "安魂 (Requiem)";
            default -> "Tier " + tier;
        };
    }

    /**
     * 删除订阅组
     * 用于后台管理接口
     *
     * @param id 订阅组ID
     */
    @Transactional
    public void deleteSubscribeGroup(Long id) {
        MissionSubscribe group = subscribeRepo.findById(id)
                .orElseThrow(() -> new ServiceException("订阅组不存在"));

        if (!group.getUsers().isEmpty()) {
            throw new ServiceException("存在关联用户，无法删除订阅组");
        }

        subscribeRepo.delete(group);
        log.info("删除订阅组成功 [id:{}]", id);
    }

    /**
     * 删除订阅用户
     * 用于后台管理接口
     *
     * @param userId 用户ID
     */
    @Transactional
    public void deleteSubscribeUser(Long userId) {
        MissionSubscribeUser user = userRepo.findById(userId)
                .orElseThrow(() -> new ServiceException("用户不存在"));

        if (!user.getCheckTypes().isEmpty()) {
            throw new ServiceException("存在关联类型，无法删除用户");
        }

        userRepo.delete(user);
        log.info("删除订阅用户成功 [id:{}]", userId);
    }

    /**
     * 删除检查类型
     * 用于后台管理接口
     *
     * @param checkTypeId 检查类型ID
     */
    @Transactional
    public void deleteCheckType(Long checkTypeId) {
        MissionSubscribeUserCheckType checkType = checkTypeRepo.findById(checkTypeId)
                .orElseThrow(() -> new ServiceException("检查类型不存在"));

        checkTypeRepo.delete(checkType);
        log.info("删除检查类型成功 [id:{}]", checkTypeId);
    }
}