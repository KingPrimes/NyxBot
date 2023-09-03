package com.nyx.bot.repo.impl.sys;

import com.nyx.bot.core.Ztree;
import com.nyx.bot.entity.sys.SysMenu;
import com.nyx.bot.entity.sys.SysUserAndMenu;
import com.nyx.bot.repo.sys.SysMenuRepository;
import com.nyx.bot.repo.sys.SysUserAndMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SysMenuService {

    @Autowired
    SysMenuRepository menuRepository;

    @Autowired
    SysUserAndMenuRepository userMenuRepository;


    /**
     * 保存菜单列表
     */
    public int save(SysMenu menu) {
        try {
            SysMenu save = menuRepository.save(menu);
            SysUserAndMenu um = new SysUserAndMenu();
            um.setMenuId(save.getMenuId());
            um.setUserId(1L);
            userMenuRepository.save(um);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 根据用户ID查询
     */
    public List<SysMenu> list(Long userId) {
        List<SysUserAndMenu> mIds = userMenuRepository.findByUserId(userId);
        List<Long> longStream = mIds.stream().map(SysUserAndMenu::getMenuId).toList();
        return menuRepository.findAllById(longStream);
    }

    /**
     * 根据菜单ID查询菜单是否被分配
     *
     * @param id 菜单ID
     */
    public SysUserAndMenu findUMById(Long id) {
        return userMenuRepository.findByMenuId(id);
    }

    /**
     * 获取所有的菜单
     */
    public List<SysMenu> list() {
        return menuRepository.findAll();
    }

    public List<SysMenu> getChildPerms() {
        return getChildPerms(list(1L), 0);
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (SysMenu t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            Optional.ofNullable(t.getParentId()).ifPresent(id -> {
                if (id == parentId) {
                    recursionFn(list, t);
                    returnList.add(t);
                }
            });
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        for (SysMenu n : list) {
            Optional.ofNullable(n.getParentId()).ifPresent(id -> {
                if (id.equals(t.getMenuId())) {
                    tlist.add(n);
                }
            });
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return !getChildList(list, t).isEmpty();
    }


    /**
     * 根据菜单ID查询
     */
    public SysMenu findById(Long id) {
        AtomicReference<SysMenu> sysMenu = new AtomicReference<>();
        menuRepository.findById(id).ifPresent(sysMenu::set);
        return sysMenu.get();
    }

    /**
     * 根据父菜单ID查询
     */
    public SysMenu findByParentId(Long parentId) {
        AtomicReference<SysMenu> sysMenu = new AtomicReference<>();
        menuRepository.findById(parentId).ifPresent(sysMenu::set);
        return sysMenu.get();
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     */
    public boolean checkMenuNameUnique(SysMenu menu) {
        SysMenu menuName = menuRepository.findByMenuName(menu.getMenuName());
        AtomicBoolean flag = new AtomicBoolean(true);

        Optional.ofNullable(menuName).ifPresent(s -> {
            if (s.getMenuName().equals(menu.getMenuName())) {
                flag.set(false);
            }
        });

        return flag.get();
    }

    /**
     * 查询所有菜单信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<Ztree> menuTreeData(Long userId) {
        List<SysMenu> menuList = menuRepository.findAll();
        return initZtree(menuList);
    }


    /**
     * 对象转菜单树
     *
     * @param menuList 菜单列表
     * @return 树结构列表
     */
    private List<Ztree> initZtree(List<SysMenu> menuList) {
        List<Ztree> ztrees = new ArrayList<>();
        for (SysMenu menu : menuList) {
            Ztree ztree = new Ztree();
            ztree.setId(menu.getMenuId());
            ztree.setPId(menu.getParentId());
            ztree.setName(menu.getMenuName());
            ztree.setTitle(menu.getMenuName());
            ztree.setChecked(false);
            ztrees.add(ztree);
        }
        return ztrees;
    }


    /**
     * 删除数据
     */
    public int remove(Long menuId) {
        menuRepository.deleteAllById(Collections.singleton(menuId));
        return 1;
    }
}
