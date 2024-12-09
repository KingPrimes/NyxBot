package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeGroupCheckType;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeUserRepository;
import com.nyx.bot.utils.SpringUtils;
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
    public static String userSubscriptions(Long botUid, Long userUid, String userName, Long subGroup, String groupName, String raw) {
        String[] split = raw.replace("订阅", "").replace(" ", "").split("-");
        int i = Integer.parseInt(split[0]);
        if (i <= 0 || i > SubscribeEnums.values().length - 1) {
            return SubscribeEnums.ERROR.getNAME();
        }
        SubscribeEnums subscribeEnums = SubscribeEnums.values()[i];
        WarframeMissionTypeEnum missionTypeEnum;
        long tierNum;
        if (split.length >= 2) {
            int te = Integer.parseInt(split[1]);
            if (te <= 0 || te > WarframeMissionTypeEnum.values().length - 1) {
                return SubscribeEnums.ERROR.getNAME();
            }
            missionTypeEnum = WarframeMissionTypeEnum.values()[te];
        } else {
            missionTypeEnum = WarframeMissionTypeEnum.ERROR;
        }
        if (split.length >= 3) {
            tierNum = Long.parseLong(split[2]);
        } else {
            tierNum = 0L;
        }

        MissionSubscribeRepository bean = SpringUtils.getBean(MissionSubscribeRepository.class);
        Optional<MissionSubscribe> missionSubscribe = bean.findById(subGroup);
        //查询已经订阅过的群组

        missionSubscribe.ifPresentOrElse(s -> {
                    List<MissionSubscribeUser> users;
                    if (s.getSubUsers().stream().anyMatch(u -> u.getUserId().equals(userUid))) {
                        users = s.getSubUsers().stream()
                                .filter(u -> u.getUserId().equals(userUid))
                                .peek(um -> {
                                    //判断是否存在订阅类型
                                    if (um.getTypeList().stream().noneMatch(t -> t.getSubscribe().equals(subscribeEnums) && t.getMissionTypeEnum().equals(missionTypeEnum))) {
                                        // 不存在添加订阅
                                        List<MissionSubscribeUserCheckType> list = um.getTypeList();
                                        MissionSubscribeUserCheckType checkType = new MissionSubscribeUserCheckType();
                                        checkType.setSubscribe(subscribeEnums);
                                        checkType.setMissionTypeEnum(missionTypeEnum);
                                        /*  checkType.setUserId(userUid);*/
                                        checkType.setTierNum(tierNum);
                                        list.add(checkType);
                                        um.setTypeList(list);
                                    }
                                }).toList();
                    } else {
                        users = s.getSubUsers();
                        users.add(saveUser(userUid, userName, subscribeEnums, missionTypeEnum, tierNum));
                    }

                    if (s.getCheckTypes().stream().noneMatch(t -> t.getSubscribe().equals(subscribeEnums))) {
                        List<MissionSubscribeGroupCheckType> types = s.getCheckTypes();
                        MissionSubscribeGroupCheckType type = new MissionSubscribeGroupCheckType();
                        type.setSubscribe(subscribeEnums);
                        type.setCheckType(true);
                        types.add(type);
                        s.setCheckTypes(types);
                    }
                    s.setSubUsers(users);
                    bean.save(s);
                },
                //从未订阅新添用户
                () -> {
                    MissionSubscribe ms = new MissionSubscribe();
                    ms.setSubGroup(subGroup);
                    ms.setGroupName(groupName);
                    ms.setSubBotUid(botUid);

                    List<MissionSubscribeUser> users = new ArrayList<>();

                    users.add(saveUser(userUid, userName, subscribeEnums, missionTypeEnum, tierNum));

                    ms.setSubUsers(users);

                    MissionSubscribeGroupCheckType gt = new MissionSubscribeGroupCheckType();
                    gt.setCheckType(true);
                    gt.setSubscribe(subscribeEnums);
                    List<MissionSubscribeGroupCheckType> gts = new ArrayList<>();
                    gts.add(gt);
                    ms.setCheckTypes(gts);
                    bean.save(ms);
                });
        return "订阅成功！";
    }

    /**
     * 取消订阅
     *
     * @param userUid  用户ID
     * @param subGroup 订阅群组
     * @param raw      源消息
     * @return 发送的信息
     */
    public static String userCancelSubscribe(Long userUid, Long subGroup, String raw) {
        StringBuffer motion = new StringBuffer();
        MissionSubscribeRepository bean = SpringUtils.getBean(MissionSubscribeRepository.class);
        Optional<MissionSubscribe> missionSubscribe = bean.findById(subGroup);
        String[] split = raw.replace("取消订阅", "").replace(" ", "").split("-");
        int i = Integer.parseInt(split[0]);
        if (i <= 0 || i > SubscribeEnums.values().length - 1) {
            return SubscribeEnums.ERROR.getNAME();
        }
        SubscribeEnums subscribeEnums = SubscribeEnums.values()[i];
        WarframeMissionTypeEnum missionTypeEnum;
        long tierNum;
        if (split.length >= 2) {
            int te = Integer.parseInt(split[1]);
            if (te <= 0 || te > WarframeMissionTypeEnum.values().length - 1) {
                return SubscribeEnums.ERROR.getNAME();
            }
            missionTypeEnum = WarframeMissionTypeEnum.values()[te];
        } else {
            missionTypeEnum = WarframeMissionTypeEnum.ERROR;
        }
        if (split.length >= 3) {
            tierNum = Long.parseLong(split[2]);
        } else {
            tierNum = 0L;
        }
        missionSubscribe.ifPresentOrElse(m -> {
            List<MissionSubscribeUser> subUsers = m.getSubUsers();
            long countUser = subUsers.stream()
                    .filter(u -> u.getUserId().equals(userUid))
                    .peek(u -> {
                                long countUserType = u.getTypeList().stream()
                                        .filter(t -> t.getSubscribe().equals(subscribeEnums) &&
                                                t.getMissionTypeEnum().equals(missionTypeEnum) &&
                                                t.getTierNum().equals(tierNum))
                                        .peek(t -> SpringUtils.getBean(MissionSubscribeUserCheckTypeRepository.class).delete(t)).count();
                                log.debug("剩余用户订阅类型：{}", countUserType);
                                if (countUserType <= 0) {
                                    SpringUtils.getBean(MissionSubscribeUserRepository.class).delete(u);
                                }
                            }
                    ).count();
            log.debug("剩余订阅人数：{}", countUser);
            if (countUser <= 0) {
                SpringUtils.getBean(MissionSubscribeRepository.class).delete(m);
            }
            motion.append("成功取消订阅！");
        }, () -> motion.append("未订阅！无需取消！"));

        return motion.toString();
    }

    private static MissionSubscribeUser saveUser(Long userUid, String userName, SubscribeEnums subscribeEnums, WarframeMissionTypeEnum missionTypeEnum, long tierNum) {
        MissionSubscribeUser user = new MissionSubscribeUser();
        user.setUserName(userName);
        user.setUserId(userUid);

        MissionSubscribeUserCheckType ut = new MissionSubscribeUserCheckType();
        ut.setSubscribe(subscribeEnums);
        ut.setMissionTypeEnum(missionTypeEnum);
        ut.setTierNum(tierNum);
        List<MissionSubscribeUserCheckType> tls = new ArrayList<>();
        tls.add(ut);
        user.setTypeList(tls);
        return user;
    }


}
