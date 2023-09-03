package com.nyx.bot.controller.system;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Ztree;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.SysMenu;
import com.nyx.bot.entity.sys.SysUserAndMenu;
import com.nyx.bot.repo.impl.sys.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/sys/menu")
public class MenuController extends BaseController {

    String prefix = "sys/menu";

    @Autowired
    SysMenuService menuService;

    @GetMapping
    public String menu() {
        return prefix + "/menu";
    }

    @PostMapping("/list")
    @ResponseBody
    public List<SysMenu> list() {
        return menuService.list();
    }


    /**
     * 新增
     */
    @GetMapping("/add/{parentId}")
    public String add(@PathVariable("parentId") Long parentId, ModelMap mmap) {
        SysMenu menu;
        if (0L != parentId) {
            menu = menuService.findByParentId(parentId);
        } else {
            menu = new SysMenu();
            menu.setMenuId(0L);
            menu.setMenuName("主目录");
        }
        mmap.put("menu", menu);
        return prefix + "/add";
    }


    /**
     * 新增保存菜单
     */
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysMenu menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        return toAjax(menuService.save(menu));
    }

    /**
     * 修改菜单
     */
    @GetMapping("/edit/{menuId}")
    public String edit(@PathVariable("menuId") Long menuId, ModelMap mmap) {
        mmap.put("menu", menuService.findById(menuId));
        return prefix + "/edit";
    }


    /**
     * 修改保存菜单
     */
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysMenu menu) {
        return toAjax(menuService.save(menu));
    }


    /**
     * 选择菜单图标
     */
    @GetMapping("/icon")
    public String icon() {
        return prefix + "/icon";
    }


    /**
     * 校验菜单名称
     */
    @PostMapping("/checkMenuNameUnique")
    @ResponseBody
    public boolean checkMenuNameUnique(SysMenu menu) {
        return menuService.checkMenuNameUnique(menu);
    }

    @GetMapping("/menuTreeData")
    @ResponseBody
    public List<Ztree> menuTreeData() {
        return menuService.menuTreeData(1L);
    }

    /**
     * 选择菜单树
     */
    @GetMapping("/selectMenuTree/{menuId}")
    public String selectMenuTree(@PathVariable("menuId") Long menuId, ModelMap mmap) {
        mmap.put("menu", menuService.findById(menuId));
        return prefix + "/tree";
    }

    /**
     * 删除菜单
     */
    @GetMapping("/remove/{menuId}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("menuId") Long menuId) {
        SysMenu byParentId = menuService.findByParentId(menuId);
        if (byParentId != null) {
            return AjaxResult.warn("存在子菜单,不允许删除");
        }
        SysUserAndMenu byId = menuService.findUMById(menuId);
        if (byId != null) {
            return AjaxResult.warn("菜单已分配,不允许删除");
        }

        return toAjax(menuService.remove(menuId));
    }

}
