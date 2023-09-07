package com.nyx.bot.repo.impl.sys;

import com.nyx.bot.core.Ztree;
import com.nyx.bot.entity.sys.SysMenu;
import com.nyx.bot.entity.sys.SysUserAndMenu;
import com.nyx.bot.repo.sys.SysMenuRepository;
import com.nyx.bot.repo.sys.SysUserAndMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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


    public void openGocq() {
        foundation(true);
    }

    public void offGocq() {
        foundation(false);
    }

    public void openIcqq() {
        foundation(true);
    }

    public void offIcqq() {
        foundation(false);
    }

    public void openWarframe() {
        List<SysUserAndMenu> ums = warframe();
        ums.forEach(u -> {
            SysUserAndMenu byMenuId = userMenuRepository.findByMenuId(u.getMenuId());
            Optional.ofNullable(byMenuId).ifPresentOrElse(b -> {
            }, () -> userMenuRepository.save(u));
        });
    }

    public void offWarframe() {
        List<SysUserAndMenu> ums = warframe();
        ums.forEach(u -> {
            SysUserAndMenu byMenuId = userMenuRepository.findByMenuId(u.getMenuId());
            Optional.ofNullable(byMenuId).ifPresent(b ->
            {
                b.setUserId(0L);
                b.setMenuId(0L);
                userMenuRepository.save(b);
            });
        });
    }

    public void openChatGpt() {
        userMenuRepository.save(new SysUserAndMenu(20L, 1L, 8L));
    }

    public void offChatGpt() {
        userMenuRepository.save(new SysUserAndMenu(20L, 0L, 0L));
    }

    public void openStableDiffusion() {
        userMenuRepository.save(new SysUserAndMenu(19L, 1L, 10L));
    }

    public void offStableDiffusion() {
        userMenuRepository.save(new SysUserAndMenu(19L, 0L, 0L));
    }

    public void openYiYan() {
        userMenuRepository.save(new SysUserAndMenu(18L, 1L, 9L));
    }

    public void offYiYan() {
        userMenuRepository.save(new SysUserAndMenu(18L, 0L, 0L));
    }

    private void foundation(boolean isOpen) {
        List<SysUserAndMenu> ums = new ArrayList<>();
        ums.add(new SysUserAndMenu(12L, 1L, 4L));
        ums.add(new SysUserAndMenu(13L, 1L, 5L));
        ums.add(new SysUserAndMenu(14L, 1L, 6L));
        ums.add(new SysUserAndMenu(15L, 1L, 7L));
        ums.add(new SysUserAndMenu(16L, 1L, 12L));
        ums.add(new SysUserAndMenu(17L, 1L, 13L));
        if (isOpen) {
            ums.forEach(u -> {
                SysUserAndMenu byMenuId = userMenuRepository.findByMenuId(u.getMenuId());
                Optional.ofNullable(byMenuId).ifPresentOrElse(b -> {
                }, () -> {
                    userMenuRepository.save(u);
                });
            });
            return;
        }
        ums.forEach(u -> {
            SysUserAndMenu byMenuId = userMenuRepository.findByMenuId(u.getMenuId());
            Optional.ofNullable(byMenuId).ifPresent(b -> {
                b.setMenuId(0L);
                b.setUserId(0L);
                userMenuRepository.save(b);
            });
        });
    }

    private List<SysUserAndMenu> warframe() {
        List<SysUserAndMenu> ums = new ArrayList<>();
        ums.add(new SysUserAndMenu(5L, 1L, 14L));
        ums.add(new SysUserAndMenu(6L, 1L, 15L));
        ums.add(new SysUserAndMenu(7L, 1L, 16L));
        ums.add(new SysUserAndMenu(8L, 1L, 17L));
        ums.add(new SysUserAndMenu(9L, 1L, 18L));
        ums.add(new SysUserAndMenu(10L, 1L, 19L));
        ums.add(new SysUserAndMenu(11L, 1L, 20L));
        return ums;
    }

}
