package com.nyx.bot.entity.sys;

import com.nyx.bot.core.dao.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class SysMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long menuId;

    /**
     * 菜单名称
     */

    String menuName;

    /**
     * 父菜单名称
     */
    String parentName;


    /**
     * 父菜单ID
     */
    Long parentId;

    /**
     * 显示顺序
     */
    String orderNum;

    /**
     * 菜单URL
     */
    String url;

    /**
     * 打开方式（menuItem页签 menuBlank新窗口）
     */
    String target;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    char menuType;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    char visible;

    /**
     * 是否刷新（0刷新 1不刷新）
     */
    char isRefresh;

    /**
     * 菜单图标
     */
    String icon;

    // 国际化
    String i18n;

    // svg 图标
    String svg;

    @Transient
    List<SysMenu> children;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("menuId", menuId)
                .append("menuName", menuName)
                .append("parentName", parentName)
                .append("parentId", parentId)
                .append("orderNum", orderNum)
                .append("url", url)
                .append("target", target)
                .append("menuType", menuType)
                .append("visible", visible)
                .append("isRefresh", isRefresh)
                .append("icon", icon)
                .append("i18n", i18n)
                .append("svg", svg)
                .append("children", children)
                .toString();
    }
}
