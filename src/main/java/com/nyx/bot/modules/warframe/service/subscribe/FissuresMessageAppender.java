package com.nyx.bot.modules.warframe.service.subscribe;

import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.ActiveMission;
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
    private List<ActiveMission> filterFissures(List<ActiveMission> fissures,
                                                       MissionSubscribeUser user) {
        return user.getCheckTypes().stream()
                .filter(check -> check.getSubscribe() == SubscribeEnums.FISSURES)
                .flatMap(check -> fissures.stream()
                        .filter(f -> {
                            if (check.getTierNum() != 0 && !check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return Objects.equals(f.getVoidEnum().ordinal(), check.getTierNum()) &&
                                        f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (!check.getMissionTypeEnum().equals(WarframeMissionTypeEnum.ERROR)) {
                                return f.getMissionType().contains(check.getMissionTypeEnum().get());
                            } else if (check.getTierNum() != 0) {
                                return Objects.equals(f.getVoidEnum().ordinal(), check.getTierNum());
                            }
                            return true;
                        }))
                .toList();
    }

    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        List<ActiveMission> filtered = filterFissures(data.getActiveMissions(), user);
        if (!filtered.isEmpty()) {
            builder.text("裂隙信息：\n");
            SystemImage.addSystemImage(builder, SubscribeEnums.FISSURES, subscribe, user, filtered);
        }
    }
}
