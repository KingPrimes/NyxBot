package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeGroupCheckType;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.repo.warframe.MissionSubscribeRepository;
import com.nyx.bot.utils.SpringUtils;

import java.util.*;
import java.util.stream.Collectors;

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


    public static String getMS(Long botUid, Long userUid, String userName, Long subGroup, String groupName, String raw) {
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
                        users.add(user);
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
                    List<MissionSubscribeUser> users = new ArrayList<>();
                    users.add(user);
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

}
