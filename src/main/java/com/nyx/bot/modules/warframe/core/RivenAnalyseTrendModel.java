package com.nyx.bot.modules.warframe.core;

import com.nyx.bot.enums.RivenTrendEnum;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Data
public class RivenAnalyseTrendModel {
    String weaponName;
    String rivenName;
    String newDot;
    Double newNum;
    String weaponType;
    List<Attribute> attributes;

    public String getNewDot() {
        if (newNum != null) {
            return RivenTrendEnum.getRivenTrendDot(newNum);
        }
        return RivenTrendEnum.RIVEN_TREND_1.getDoc();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("weaponName", weaponName)
                .append("rivenName", rivenName)
                .append("newDot", newDot)
                .append("newNum", newNum)
                .append("weaponType", weaponType)
                .append("attributes", attributes)
                .toString();
    }

    @Data
    public static class Attribute {
        String name;
        String attributeName;
        Double attr;
        String lowAttr;
        String highAttr;

        String attrDiff;

        String lowAttrDiff;

        String highAttrDiff;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("name", name)
                    .append("attr", attr)
                    .append("attributeName", attributeName)
                    .append("lowAttr", lowAttr)
                    .append("highAttr", highAttr)
                    .append("attrDiff", attrDiff)
                    .append("lowAttrDiff", lowAttrDiff)
                    .append("highAttrDiff", highAttrDiff)
                    .toString();
        }
    }
}
