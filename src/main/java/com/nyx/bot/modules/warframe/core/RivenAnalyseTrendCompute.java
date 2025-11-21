package com.nyx.bot.modules.warframe.core;

import com.nyx.bot.utils.DoubleUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class RivenAnalyseTrendCompute {
    String weaponsName;
    String rivenName;
    List<Attribute> attributes = new ArrayList<>();

    public boolean add(Attribute attribute) {
        return attributes.add(attribute);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("weaponsName", weaponsName)
                .append("rivenName", rivenName)
                .append("attributes", attributes)
                .toString();
    }

    public static class Attribute {
        @Getter
        @Setter
        String name;
        @Getter
        @Setter
        String attributeName;

        @Setter
        @Getter
        Double attribute;

        @Getter
        @Setter
        Boolean nag;

        Double lowAttribute = 0.0;

        Double highAttribute = 0.0;


        /**
         * 根据中间值计算紫卡的数据 高低
         *
         * @return html文本
         */
        public String getAttributeDiff() {
            double medianValue = (lowAttribute + highAttribute) / 2;
            log.debug("中间值：{}", medianValue);
            double i = Math.abs(attribute) - Math.abs(medianValue);
            return getString(medianValue, attribute, i);
        }

        /**
         * 根据中间值计算紫卡的歧视词条数据 高低
         *
         * @return html 文本
         */
        public String getAttributeDiscriminationDiff() {
            double medianValue = (lowAttribute + highAttribute) / 2;
            log.debug("歧视 - 中间值：{}", Math.abs(medianValue));
            double abs = Math.abs(attribute);
            if (abs > 1) {
                abs = (abs - 1) * 100;
            } else {
                abs = 100 - (abs * 100);
            }
            log.info("abs:{}", abs);
            double i = abs - Math.abs(medianValue);
            return getString(medianValue, abs, i);
        }

        private String getString(double medianValue, double abs, double i) {
            double v = DoubleUtils.formatDouble4(((Math.abs(medianValue) - Math.abs(abs)) / Math.abs(medianValue)) * 100);
            return i > 0 ? "+" + Math.abs(v) + "%" :
                    i == 0 ?
                            "0.0%" :
                            "-" + Math.abs(v) + "%";
        }


        // 计算公式 具体数值 = 浮动系数 * 基础数值 * 倾向值 * 属性种类修正系数
        // 浮动系数 0.9 - 1.1
        //              正面修正系数      负面修正系数
        // 2正            0.99            0
        // 2正1负         1.2375          -0.495
        // 3正            0.75            0
        // 3正1负         0.9375          -0.75


        /**
         * 获取最低数值
         *
         * @param baseVal 基础数值
         * @param pro     紫卡倾向
         * @param x       词条数目
         * @param nag     是否携带负属性
         * @param isNag   是否是负属性词条
         * @return 计算结果
         */
        public Double getLowAttribute(Double baseVal, Double pro, int x, boolean nag, boolean isNag) {
            switch (x) {
                case 2:
                    lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.99);
                    break;
                case 3:
                    if (nag) {
                        if (isNag) {
                            lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * -0.495);
                            break;
                        }
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 1.2375);
                    } else {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.75);
                    }
                    break;
                case 4: {
                    if (isNag) {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * -0.75);
                    } else {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.9375);
                    }
                    break;
                }
            }
            return lowAttribute;
        }

        /**
         * 获取最高数值
         *
         * @param baseVal 基础数值
         * @param pro     紫卡倾向
         * @param x       词条数目
         * @param nag     是否携带负属性
         * @param isNag   是否是负属性词条
         * @return 计算结果
         */
        public Double getHighAttribute(Double baseVal, Double pro, int x, boolean nag, boolean isNag) {
            switch (x) {
                case 2:
                    highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 0.99);
                    break;
                case 3:
                    if (nag) {
                        if (isNag) {
                            highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * -0.495);
                            break;
                        }
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 1.2375);
                    } else {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 0.75);
                    }
                    break;
                case 4: {
                    if (isNag) {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * -0.75);
                    } else {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 0.9375);
                    }
                    break;
                }
            }
            return highAttribute;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("name", name)
                    .append("attribute", attribute)
                    .append("attributeName", attributeName)
                    .append("nag", nag)
                    .append("lowAttribute", lowAttribute)
                    .append("highAttribute", highAttribute)
                    .toString();
        }
    }
}
