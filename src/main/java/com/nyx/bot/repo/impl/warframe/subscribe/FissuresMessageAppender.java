package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.onebot.Msg;

import java.util.List;
import java.util.Objects;

public class FissuresMessageAppender implements MessageAppender{

    /**
     * 过滤裂隙数据
     * @param fissures 裂隙数据
     * @param user 用户
     * @return 过滤后的裂隙数据
     */
    private List<GlobalStates.Fissures> filterFissures(List<GlobalStates.Fissures> fissures,
                                                       MissionSubscribeUser user) {
        return user.getCheckTypes().stream()
                .filter(check -> check.getSubscribe() == SubscribeEnums.FISSURES)
                .flatMap(check -> fissures.stream()
                        .filter(f -> {
                            if (check.getTierNum() != 0 && !check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return Objects.equals(f.getTierNum(), check.getTierNum()) &&
                                        f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (!check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (check.getTierNum() != 0) {
                                return Objects.equals(f.getTierNum(), check.getTierNum());
                            }
                            return true;
                        }))
                .toList();
    }

    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        List<GlobalStates.Fissures> filtered = filterFissures(data.getFissures(), user);
        if (!filtered.isEmpty()) {
            builder.text("裂隙信息：\n");
            SystemImage.addSystemImage(builder, SubscribeEnums.FISSURES, subscribe, user, filtered);
        }
    }
}
