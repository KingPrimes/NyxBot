package com.nyx.bot.entity.warframe;

import com.nyx.bot.utils.DoubleUtils;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

@Data
public class RivenAnaiyseTrend {
    String weaponsName;
    String rivenName;
    List<Attribute> c = new ArrayList<>();

    public boolean add(Attribute attribute) {
        return c.add(attribute);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("weaponsName", weaponsName)
                .append("rivenName", rivenName)
                .append("c", c)
                .toString();
    }

    public static class Attribute {
        String name;
        Double attribute;

        Boolean nag;

        Double lowAttribute = 0.0;

        Double highAttribute = 0.0;

        /**
         * 正属性计算对比最低值高多少
         *
         * @return 差
         */
        public Double getLowAttributeDiff() {
            if (lowAttribute.equals(0.0)) {
                return lowAttribute;
            }
            return DoubleUtils.formatDouble4(attribute - lowAttribute);
        }

        /**
         * 负属性计算 对比最高值低多少
         *
         * @return 差
         */
        public Double getHighAttributeDiff() {
            if (highAttribute.equals(0.0)) {
                return highAttribute;
            }
            return DoubleUtils.formatDouble4(highAttribute - attribute);
        }

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
                        break;
                    } else {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.75);
                        break;
                    }
                case 4: {
                    if (isNag) {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * -0.75);
                        break;
                    } else {
                        lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.9375);
                        break;
                    }
                }
                default: {
                    switch (x - 1) {
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
                                break;
                            } else {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.75);
                                break;
                            }
                        case 4: {
                            if (isNag) {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * -0.75);
                                break;
                            } else {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.9375);
                                break;
                            }
                        }
                    }
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
                        break;
                    } else {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 0.75);
                        break;
                    }
                case 4: {
                    if (isNag) {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * -0.75);
                    } else {
                        highAttribute = DoubleUtils.formatDouble4(1.1 * baseVal * pro * 0.9375);
                    }
                    break;
                }
                default: {
                    switch (x - 1) {
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
                                break;
                            } else {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.75);
                                break;
                            }
                        case 4: {
                            if (isNag) {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * -0.75);
                                break;
                            } else {
                                lowAttribute = DoubleUtils.formatDouble4(0.9 * baseVal * pro * 0.9375);
                                break;
                            }
                        }
                    }
                }
            }
            return highAttribute;
        }

        public Boolean getNag() {
            return nag;
        }

        public void setNag(Boolean nag) {
            this.nag = nag;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getAttribute() {
            return attribute;
        }

        public void setAttribute(Double attribute) {
            this.attribute = attribute;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("name", name)
                    .append("attribute", attribute)
                    .append("nag", nag)
                    .append("lowAttribute", lowAttribute)
                    .append("highAttribute", highAttribute)
                    .toString();
        }
    }
}
