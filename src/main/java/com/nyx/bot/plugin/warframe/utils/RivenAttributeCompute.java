package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.entity.warframe.RivenAnalyseTrend;
import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.plugin.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.plugin.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.RivenAnalyseTrendRepository;
import com.nyx.bot.repo.warframe.RivenTrendRepository;
import com.nyx.bot.utils.MatchUtil;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RivenAttributeCompute {

    public static String ocrRivenCompute(AnyMessageEvent event) {
        //识别图片
        List<List<String>> ocrImages = OCRImage.ocrImage(event);
        //处理识别数据
        List<RivenAnalyseTrendCompute> riven = getRiven(ocrImages);
        //计算紫卡加成属性
        List<List<RivenAnalyseTrendModel>> models = setAttributeNumber(riven);
        //转换成Json格式数据
        return JSON.toJSONString(models);
    }

    /**
     * 处理识别数据
     */
    private static List<RivenAnalyseTrendCompute> getRiven(List<List<String>> images) {
        List<RivenAnalyseTrendCompute> trends = new ArrayList<>();
        for (List<String> image : images) {
            RivenAnalyseTrendCompute trend = new RivenAnalyseTrendCompute();
            for (String s : image) {
                if (s.length() < 3) {
                    continue;
                }
                if (MatchUtil.isWeaponsName(s)) {
                    trend.setWeaponsName(MatchUtil.getChines(s));
                    if (MatchUtil.isRivenNameEx(s)) {
                        trend.setRivenName(MatchUtil.getRivenNameE(s));
                    }
                }
                if (MatchUtil.isRivenNameEx(s)) {
                    if (trend.getRivenName() == null) {
                        trend.setRivenName(MatchUtil.getRivenNameE(s));
                    } else {
                        if (!trend.getRivenName().equals(MatchUtil.getRivenNameE(s))) {
                            trend.setRivenName(trend.getRivenName() + MatchUtil.getRivenNameE(s));
                        }
                    }
                }
                if (MatchUtil.isAttribute(s)) {
                    if (s.contains("入")) {
                        s = s.replace("入", "");
                    }
                    RivenAnalyseTrendCompute.Attribute attribute = new RivenAnalyseTrendCompute.Attribute();
                    if (s.contains("射速")) {
                        attribute.setName(MatchUtil.getAttribetName(s) + " 效果加倍）");
                    } else {
                        attribute.setName(MatchUtil.getAttribetName(s));
                    }
                    attribute.setAttribute(MatchUtil.getAttributeNum(s));
                    attribute.setNag(attribute.getAttribute() < 0);
                    trend.add(attribute);
                }
            }
            trends.add(trend);
        }
        return trends;
    }

    /**
     * 计算紫卡加成数据
     */
    private static List<List<RivenAnalyseTrendModel>> setAttributeNumber(List<RivenAnalyseTrendCompute> trends) {
        //存放多张紫卡
        List<List<RivenAnalyseTrendModel>> rives = new ArrayList<>();
        //遍历识别处理后的数据
        for (RivenAnalyseTrendCompute trend : trends) {
            //查询武器的英文名称
            String weaponsName_En = SpringUtils.getBean(TranslationService.class).zhToEn(trend.getWeaponsName());
            //具体的紫卡倾向
            List<RivenTrend> likeTrendName = SpringUtils.getBean(RivenTrendRepository.class).findLikeTrendName(weaponsName_En);
            //存放计算完毕的紫卡
            List<RivenAnalyseTrendModel> models = new ArrayList<>();
            //遍历查询到的所有武器
            for (RivenTrend rivenTrend : likeTrendName) {
                RivenAnalyseTrendModel model = new RivenAnalyseTrendModel();
                RivenTrendTypeEnum weaponsType = rivenTrend.getType();
                model.setWeaponName(rivenTrend.getTraCh());
                model.setRivenName(trend.getRivenName());
                model.setNewDot(rivenTrend.getNewDot());
                model.setNewNum(rivenTrend.getNewNum());
                model.setOldDot(rivenTrend.getOldDot());
                model.setOldNum(rivenTrend.getOldNum());
                model.setWeaponType(weaponsType.getDesc());
                //具体词条数据
                List<RivenAnalyseTrendModel.Attribute> attributes = new ArrayList<>();
                //计算各数值的数据
                for (RivenAnalyseTrendCompute.Attribute attribute : trend.getAttributes()) {
                    RivenAnalyseTrend analyseTrend;
                    //判断词条是否包含 射速、攻击速度
                    if (attribute.getName().contains("射速") || attribute.getName().contains("攻击速度")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("射速/攻击速度");
                    } else if (attribute.getName().equals("伤害") || attribute.getName().equals("近战伤害")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("伤害/近战伤害");
                    } else if (attribute.getName().equals("武器后坐力")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("后坐力");
                    } else if (attribute.getName().contains("Infested")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("对Infested伤害");
                    } else if (attribute.getName().contains("Corpus")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("对Corpus伤害");
                    } else if (attribute.getName().contains("Grinner")) {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName("对Grinner伤害");
                    } else {
                        analyseTrend = SpringUtils.getBean(RivenAnalyseTrendRepository.class).findByName(attribute.getName());
                    }
                    RivenAnalyseTrendModel.Attribute attributeModel = new RivenAnalyseTrendModel.Attribute();
                    attributeModel.setAttr(attribute.getAttribute());
                    attributeModel.setName(attribute.getName());
                    boolean isNag = attribute.getAttribute() < 0;
                    switch (weaponsType) {
                        case MELEE -> {
                            attributeModel.setLowAttr(
                                    String.valueOf(
                                            attribute.getLowAttribute(
                                                    analyseTrend.getMelle(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            attributeModel.setHighAttr(
                                    String.valueOf(
                                            attribute.getHighAttribute(
                                                    analyseTrend.getMelle(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );

                            if (!isNag) {
                                attributeModel.setHighAttrDiff(
                                        String.valueOf(
                                                attribute.getLowAttributeDiff()
                                        )
                                );
                            } else {
                                attributeModel.setLowAttrDiff(
                                        String.valueOf(
                                                attribute.getHighAttributeDiff()
                                        )
                                );
                            }
                        }
                        case RIFLE -> {
                            attributeModel.setLowAttr(
                                    String.valueOf(
                                            attribute.getLowAttribute(
                                                    analyseTrend.getRifle(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            attributeModel.setHighAttr(
                                    String.valueOf(
                                            attribute.getHighAttribute(
                                                    analyseTrend.getRifle(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );

                            if (!isNag) {
                                attributeModel.setHighAttrDiff(
                                        String.valueOf(
                                                attribute.getLowAttributeDiff()
                                        )
                                );
                            } else {
                                attributeModel.setLowAttrDiff(
                                        String.valueOf(
                                                attribute.getHighAttributeDiff()
                                        )
                                );
                            }
                        }
                        case PISTOL -> {
                            attributeModel.setLowAttr(
                                    String.valueOf(
                                            attribute.getLowAttribute(
                                                    analyseTrend.getPistol(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            attributeModel.setHighAttr(
                                    String.valueOf(
                                            attribute.getHighAttribute(
                                                    analyseTrend.getPistol(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );

                            if (!isNag) {
                                attributeModel.setHighAttrDiff(
                                        String.valueOf(
                                                attribute.getLowAttributeDiff()
                                        )
                                );
                            } else {
                                attributeModel.setLowAttrDiff(
                                        String.valueOf(
                                                attribute.getHighAttributeDiff()
                                        )
                                );
                            }
                        }
                        case ARCHGUN -> {
                            attributeModel.setLowAttr(
                                    String.valueOf(
                                            attribute.getLowAttribute(
                                                    analyseTrend.getArchwing(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            attributeModel.setHighAttr(
                                    String.valueOf(
                                            attribute.getHighAttribute(
                                                    analyseTrend.getArchwing(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );

                            if (!isNag) {
                                attributeModel.setHighAttrDiff(
                                        String.valueOf(
                                                attribute.getLowAttributeDiff()
                                        )
                                );
                            } else {
                                attributeModel.setLowAttrDiff(
                                        String.valueOf(
                                                attribute.getHighAttributeDiff()
                                        )
                                );
                            }
                        }
                        case SHOTGUN -> {
                            attributeModel.setLowAttr(
                                    String.valueOf(
                                            attribute.getLowAttribute(
                                                    analyseTrend.getShotgun(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            attributeModel.setHighAttr(
                                    String.valueOf(
                                            attribute.getHighAttribute(
                                                    analyseTrend.getShotgun(),
                                                    rivenTrend.getNewNum(),
                                                    trend.getAttributes().size(),
                                                    attribute.getNag(),
                                                    isNag
                                            )
                                    )
                            );
                            if (!isNag) {
                                attributeModel.setHighAttrDiff(
                                        String.valueOf(
                                                attribute.getLowAttributeDiff()
                                        )
                                );
                            } else {
                                attributeModel.setLowAttrDiff(
                                        String.valueOf(
                                                attribute.getHighAttributeDiff()
                                        )
                                );
                            }
                        }
                    }
                    attributes.add(attributeModel);
                }
                model.setAttributes(attributes);
                models.add(model);
            }
            rives.add(models);
        }
        return rives;
    }
}
