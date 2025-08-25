package com.nyx.bot.modules.warframe.utils;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.modules.warframe.entity.RivenAnalyseTrend;
import com.nyx.bot.modules.warframe.entity.RivenTrend;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendCompute;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.service.TranslationService;
import com.nyx.bot.modules.warframe.repo.RivenAnalyseTrendRepository;
import com.nyx.bot.modules.warframe.repo.RivenTrendRepository;
import com.nyx.bot.utils.RivenMatcherUtil;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public class RivenAttributeCompute {

    public static String ocrRivenCompute(AnyMessageEvent event) {
        //识别图片
        List<List<String>> ocrImages = OCRImage.ocrImage(event);
        log.debug("识别文字：{}\n", ocrImages);
        //处理识别数据
        List<RivenAnalyseTrendCompute> riven = getRiven(ocrImages);
        log.debug("处理后得数据:{}\n", riven);
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
            //紫卡类
            RivenAnalyseTrendCompute trend = new RivenAnalyseTrendCompute();
            // 紫卡词条集合
            List<String> attributes = new ArrayList<>();
            // 获取紫卡属性
            for (String s : image) {
                if (s.length() < 3) {
                    continue;
                }
                // 武器名称
                if (RivenMatcherUtil.isWeaponsName(s)) {
                    if (s.contains("&")) {
                        int i = s.indexOf("&");
                        String star = s.substring(i, i + 1);
                        String end = s.substring(i - 1, i);
                        if (!star.contains(" ") && !end.contains(" ")) {
                            s = s.replace("&", " & ");
                        }
                    }
                    trend.setWeaponsName(RivenMatcherUtil.getChines(s));
                    if (RivenMatcherUtil.isRivenNameEx(s)) {
                        trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                    }
                }
                // 紫卡名称
                if (RivenMatcherUtil.isRivenNameEx(s)) {
                    if (trend.getRivenName() == null) {
                        trend.setRivenName(RivenMatcherUtil.getRivenNameE(s));
                    } else {
                        if (!trend.getRivenName().equals(RivenMatcherUtil.getRivenNameE(s))) {
                            trend.setRivenName(trend.getRivenName() + RivenMatcherUtil.getRivenNameE(s));
                        }
                    }
                }
                // 紫卡词条
                if (RivenMatcherUtil.isAttribute(s)) {
                    s = s.replace(" ", "").trim();
                    if (s.contains("入")) {
                        s = s.replace("入", "");
                    }
                    attributes.add(s);
                }
            }
            boolean nag = false;
            // 判断紫卡词条是否大于等于3条
            if (attributes.size() >= 3) {
                String s = attributes.get(attributes.size() - 1);
                if (RivenMatcherUtil.getAttributeNum(s) < 0 || RivenMatcherUtil.whetherItIsDiscrimination(s)) {
                    nag = true;
                }
            }
            // 设置具体的词条属性
            for (String s : attributes) {
                RivenAnalyseTrendCompute.Attribute attribute = new RivenAnalyseTrendCompute.Attribute();
                if (s.contains("射速")) {
                    attribute.setAttributeName(s + " 效果加倍）");
                    attribute.setName(RivenMatcherUtil.getAttributeName(s) + " 效果加倍）");
                } else if (s.contains("暴击几率（")) {
                    attribute.setAttributeName(s.replaceAll("（重?击?时?", "(").trim() + "重击时 x2)");
                    attribute.setName("暴击几率（重击时 x2）");
                } else if (s.contains("滑行攻击")) {
                    attribute.setAttributeName(s.replaceAll("滑.*+", "滑行攻击暴击几率"));
                    attribute.setName("滑行攻击暴击几率");
                } else {
                    attribute.setAttributeName(s);
                    attribute.setName(RivenMatcherUtil.getAttributeName(s));
                }
                attribute.setAttribute(RivenMatcherUtil.getAttributeNum(s));
                /* attribute.setNag(MatchUtil.getAttributeNum(s) < 0)*//*|| (MatchUtil.whetherItIsDiscrimination(s) && MatchUtil.getAttributeNum(s) > 0))*//*;*/
                // 用于判断是否携带负属性
                attribute.setNag(nag);
                trend.add(attribute);
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
                IntStream.range(0, trend.getAttributes().size())
                        .mapToObj(index -> new AbstractMap.SimpleEntry<>(index, trend.getAttributes().get(index)))
                        .forEach(entry -> {
                            int index = entry.getKey();
                            RivenAnalyseTrendCompute.Attribute attribute = entry.getValue();
                            String name = attribute.getName();
                            Optional<RivenAnalyseTrend> analyseTrend;
                            RivenAnalyseTrendRepository rivenTrendRepository = SpringUtils.getBean(RivenAnalyseTrendRepository.class);
                            //判断词条是否包含 射速、攻击速度
                            if (name.contains("射速") || name.contains("攻击速度")) {
                                analyseTrend = rivenTrendRepository.findByName("射速/攻击速度");
                            } else if (name.equals("伤害") || name.equals("近战伤害")) {
                                analyseTrend = rivenTrendRepository.findByName("伤害/近战伤害");
                            } else if (name.equals("武器后坐力")) {
                                analyseTrend = rivenTrendRepository.findByName("后坐力");
                            } else if (name.contains("Infested") || name.contains("lnfested")) {
                                analyseTrend = rivenTrendRepository.findByName("对Infested伤害");
                            } else if (name.contains("Corpus")) {
                                analyseTrend = rivenTrendRepository.findByName("对Corpus伤害");
                            } else if (name.contains("Grinner") || name.contains("Grineer")) {
                                analyseTrend = rivenTrendRepository.findByName("对Grineer伤害");
                            } else if (name.contains("暴击几率") && !name.contains("滑行")) {
                                analyseTrend = rivenTrendRepository.findByName("暴击几率");
                            } else if (name.contains("秒连击持续时间")) {
                                analyseTrend = rivenTrendRepository.findByName("连击持续时间");
                            } else if (name.contains("冰冻")) {
                                analyseTrend = rivenTrendRepository.findByName("冰冻伤害");
                            } else if (name.contains("毒素")) {
                                analyseTrend = rivenTrendRepository.findByName("毒素伤害");
                            } else if (name.contains("电击")) {
                                analyseTrend = rivenTrendRepository.findByName("电击伤害");
                            } else if (name.contains("火焰")) {
                                analyseTrend = rivenTrendRepository.findByName("火焰伤害");
                            } else if (name.contains("冲击")) {
                                analyseTrend = rivenTrendRepository.findByName("冲击伤害");
                            } else if (name.contains("切割")) {
                                analyseTrend = rivenTrendRepository.findByName("切割伤害");
                            } else if (name.contains("穿刺")) {
                                analyseTrend = rivenTrendRepository.findByName("穿刺伤害");
                            } else if (name.contains("投射物")) {
                                analyseTrend = rivenTrendRepository.findByName("投射物飞行速度");
                            } else {
                                analyseTrend = rivenTrendRepository.findByName(attribute.getName());
                            }
                            RivenAnalyseTrendModel.Attribute attributeModel = new RivenAnalyseTrendModel.Attribute();
                            attributeModel.setAttributeName(attribute.getAttributeName());
                            attributeModel.setAttr(attribute.getAttribute());
                            attributeModel.setName(attribute.getName());
                            RivenAnalyseTrend analyseTrendT = analyseTrend.orElse(new RivenAnalyseTrend());
                            /*boolean isNag = attribute.getAttribute() < 0 *//*|| (MatchUtil.whetherItIsDiscrimination(attribute.getAttributeName()) && attribute.getAttribute() > 0)*//*;*/
                            // 用于判断最后一个词条是否是歧视属性
                            boolean isNag = index >= 2 ? RivenMatcherUtil.whetherItIsDiscrimination(attribute.getAttributeName()) || attribute.getAttribute() < 0 : attribute.getAttribute() < 0;
                            log.debug("当前武器名称：{}", rivenTrend.getTraCh());
                            log.debug("当前下标是否大于等于2：{}", index >= 2);
                            log.debug("当前属性是否是歧视属性:{}", RivenMatcherUtil.whetherItIsDiscrimination(attribute.getAttributeName()));
                            log.debug("当前属性是否时负数：{}", attribute.getAttribute() < 0);
                            log.debug("当前属性是否是负属性：{} --- 当前下标:{} -- 当前属性值:{} ---当前属性名称：{}\n", isNag, index, attribute.getAttribute(), attribute.getAttributeName());

                            switch (weaponsType) {
                                case MELEE -> {
                                    attributeModel.setLowAttr(
                                            String.valueOf(
                                                    attribute.getLowAttribute(
                                                            analyseTrendT.getMelle(),
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
                                                            analyseTrendT.getMelle(),
                                                            rivenTrend.getNewNum(),
                                                            trend.getAttributes().size(),
                                                            attribute.getNag(),
                                                            isNag
                                                    )
                                            )
                                    );
                                    attributeModel.setAttrDiff(attrDiff(attribute));
                                }
                                case RIFLE -> {
                                    attributeModel.setLowAttr(
                                            String.valueOf(
                                                    attribute.getLowAttribute(
                                                            analyseTrendT.getRifle(),
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
                                                            analyseTrendT.getRifle(),
                                                            rivenTrend.getNewNum(),
                                                            trend.getAttributes().size(),
                                                            attribute.getNag(),
                                                            isNag
                                                    )
                                            )
                                    );

                                    attributeModel.setAttrDiff(attrDiff(attribute));
                                }
                                case PISTOL -> {
                                    attributeModel.setLowAttr(
                                            String.valueOf(
                                                    attribute.getLowAttribute(
                                                            analyseTrendT.getPistol(),
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
                                                            analyseTrendT.getPistol(),
                                                            rivenTrend.getNewNum(),
                                                            trend.getAttributes().size(),
                                                            attribute.getNag(),
                                                            isNag
                                                    )
                                            )
                                    );
                                    attributeModel.setAttrDiff(attrDiff(attribute));
                                }
                                case ARCHGUN -> {
                                    attributeModel.setLowAttr(
                                            String.valueOf(
                                                    attribute.getLowAttribute(
                                                            analyseTrendT.getArchwing(),
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
                                                            analyseTrendT.getArchwing(),
                                                            rivenTrend.getNewNum(),
                                                            trend.getAttributes().size(),
                                                            attribute.getNag(),
                                                            isNag
                                                    )
                                            )
                                    );
                                    attributeModel.setAttrDiff(attrDiff(attribute));
                                }
                                case SHOTGUN -> {
                                    attributeModel.setLowAttr(
                                            String.valueOf(
                                                    attribute.getLowAttribute(
                                                            analyseTrendT.getShotgun(),
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
                                                            analyseTrendT.getShotgun(),
                                                            rivenTrend.getNewNum(),
                                                            trend.getAttributes().size(),
                                                            attribute.getNag(),
                                                            isNag
                                                    )
                                            )
                                    );
                                    attributeModel.setAttrDiff(attrDiff(attribute));
                                }
                            }
                            attributes.add(attributeModel);
                        });
                model.setAttributes(attributes);
                models.add(model);
            }
            rives.add(models);
        }
        return rives;
    }

    // 计算紫卡属性的高低
    private static String attrDiff(RivenAnalyseTrendCompute.Attribute attribute) {
        String name = attribute.getName();
        if (name.contains("Infested") || name.contains("Corpus") || name.contains("Grinner") || name.contains("Grineer")) {
            return attribute.getAttributeDiscriminationDiff();
        } else {
            return attribute.getAttributeDiff();
        }
    }
}
