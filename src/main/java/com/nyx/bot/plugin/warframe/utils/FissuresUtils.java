package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Slf4j
public class FissuresUtils {
    @NotNull
    public static List<GlobalStates.Fissures> getFissures(Integer type) throws DataNotInfoException {
        GlobalStates sgs = CacheUtils.getGlobalState();
        List<GlobalStates.Fissures> fissures = sgs.getFissures();
        List<GlobalStates.Fissures> list = new ArrayList<>();
        //分级
        switch (type) {
            //裂隙
            case 0 -> fissures.forEach(f -> {
                if (f.getActive()) {
                    if (!f.getIsStorm() && !f.getIsHard()) {
                        list.add(f);
                    }
                }
            });
            //九重天
            case 1 -> fissures.forEach(f -> {
                if (f.getActive()) {
                    if (f.getIsStorm()) {
                        list.add(f);
                    }
                }
            });
            //钢铁
            case 2 -> fissures.forEach(f -> {
                if (f.getActive()) {
                    if (f.getIsHard()) {
                        list.add(f);
                    }
                }
            });
            default -> fissures.forEach(f -> {
                if (f.getActive()) {
                    list.add(f);
                }
            });
        }
        return list;
    }


    public static void SortForTierNum(List<GlobalStates.Fissures> list) {
        list.sort(Comparator.comparing(GlobalStates.Fissures::getTierNum));
    }

    public static void TranslateFissures(List<GlobalStates.Fissures> list) {
        TranslationService trans = SpringUtils.getBean(TranslationService.class);
        //翻译
        list.forEach(f -> {
            String node = f.getNode();
            if (f.getIsStorm()) {
                f.setNode(trans.enToZh(
                        StringUtils.substring(
                                node,
                                0,
                                node.indexOf('('))) +
                        "(" +
                        trans.enToZh(
                                StringUtils.substring(
                                        node,
                                        node.indexOf('('),
                                        node.indexOf(')')).replace("(", "").trim())
                        + "比邻星)");
            } else {
                f.setNode(trans.enToZh(
                        StringUtils.substring(
                                node,
                                0,
                                node.indexOf('('))) +
                        "(" +
                        trans.enToZh(
                                StringUtils.substring(
                                        node,
                                        node.indexOf('('),
                                        node.indexOf(')')).replace("(", "").trim())
                        + ")");
            }
            f.setMissionType(trans.enToZh(f.getMissionType()));
            f.setMissionKey(trans.enToZh(f.getMissionKey()));
            f.setTier(trans.enToZh(f.getTier()));
            f.setEta(DateUtils.getDiff(f.getExpiry(), new Date()));
        });
    }

    /**
     * 根据订阅信息获取裂隙
     *
     * @param types 订阅信息
     * @return 裂隙列表 JSON
     */
    public static List<GlobalStates.Fissures> getSubFissures(List<MissionSubscribeUserCheckType> types, List<GlobalStates.Fissures> fissures) {
        if (fissures.isEmpty()) {
            return new ArrayList<>();
        }
        List<GlobalStates.Fissures> list = fissures;
        if (!types.isEmpty()) {
            list = new ArrayList<>(fissures.stream().filter(f ->
                            types.stream().anyMatch(
                                    t -> (t.getMissionTypeEnum() == WarframeMissionTypeEnum.ERROR || f.getMissionType().toUpperCase().contains(t.getMissionTypeEnum().name().toUpperCase()))
                                            &&
                                            (t.getTierNum() == 0 || t.getTierNum().equals(f.getTierNum()))
                            )
                    )
                    .toList());
        }
        SortForTierNum(list);
        TranslateFissures(list);
        return list;
    }
}
