package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.exprot.NodesRepository;
import com.nyx.bot.res.worldstate.ActiveMission;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FissuresUtils {

    public static List<ActiveMission> getFissures(Integer type) throws DataNotInfoException {
        //分级
        switch (type) {
            //裂隙
            case 0 -> {
                return WarframeCache.getWarframeStatus().getActiveMissions().stream()
                        .filter(m -> !Objects.nonNull(m.getHard()))
                        .peek(m -> {
                            SpringUtils.getBean(NodesRepository.class).findById(m.getNode()).ifPresent(nodes -> {
                                m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                            });
                        }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
            }
            //九重天
            case 1 -> {
                return WarframeCache.getWarframeStatus().getVoidStorms().stream()
                        .map(v -> {
                            ActiveMission am = new ActiveMission();
                            SpringUtils.getBean(NodesRepository.class).findById(v.getNode()).ifPresentOrElse(nodes -> {
                                am.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                                am.setMissionType(nodes.getMissionType());
                            }, () -> {
                                am.setNode(v.getNode());
                            });
                            am.set_id(v.get_id());
                            am.setActivation(v.getActivation());
                            am.setExpiry(v.getExpiry());
                            am.setModifier(v.getVoidEnum());
                            return am;
                        })
                        .sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
            }

            //钢铁
            case 2 -> {
                return WarframeCache.getWarframeStatus().getActiveMissions().stream()
                        .filter(m -> Objects.nonNull(m.getHard()) && m.getHard())
                        .peek(m -> {
                            SpringUtils.getBean(NodesRepository.class).findById(m.getNode()).ifPresent(nodes -> {
                                m.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                            });
                        }).sorted(Comparator.comparing(ActiveMission::getVoidEnum)).toList();
            }

            default -> {
                return new ArrayList<>();
            }
        }
    }


    public static void SortForTierNum(List<ActiveMission> list) {
        list.sort(Comparator.comparing(ActiveMission::getVoidEnum));
    }

    public static void TranslateFissures(List<ActiveMission> list) {
        TranslationService trans = SpringUtils.getBean(TranslationService.class);
        //翻译
        list.forEach(f -> {
            String node = f.getNode();
            String sb = trans.enToZh(
                    StringUtils.substring(
                            node,
                            0,
                            node.indexOf('('))) +
                    "(" +
                    trans.enToZh(
                            StringUtils.substring(
                                    node,
                                    node.indexOf('('),
                                    node.indexOf(')')).replace("(", "").trim()) +
                    ")";
            f.setNode(sb);
//            f.setMissionType(trans.enToZh(f.getMissionType()));
//            f.setMissionKey(trans.enToZh(f.getMissionKey()));
//            f.setTier(trans.enToZh(f.getTier()));
//            f.setEta(DateUtils.getDiff(f.getExpiry(), new Date()));
        });
    }

    /**
     * 根据订阅信息获取裂隙
     *
     * @param types 订阅信息
     * @return 裂隙列表 JSON
     */
    public static List<ActiveMission> getSubFissures(List<MissionSubscribeUserCheckType> types, List<ActiveMission> fissures) {
        if (fissures.isEmpty()) {
            return new ArrayList<>();
        }
//        List<GlobalStates.Fissures> list = fissures;
//        if (!types.isEmpty()) {
//            list = new ArrayList<>(fissures.stream().filter(f ->
//                            types.stream().anyMatch(
//                                    t -> (t.getMissionTypeEnum() == WarframeMissionTypeEnum.ERROR || f.getMissionType().toUpperCase().contains(t.getMissionTypeEnum().name().toUpperCase()))
//                                            &&
//                                            (t.getTierNum() == 0 || t.getTierNum().equals(f.getTierNum()))
//                            )
//                    )
//                    .toList());
//        }
//        SortForTierNum(list);
//        TranslateFissures(list);
        return new ArrayList<>();
    }
}
