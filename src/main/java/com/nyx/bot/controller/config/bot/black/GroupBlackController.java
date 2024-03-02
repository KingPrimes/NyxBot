package com.nyx.bot.controller.config.bot.black;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.black.GroupBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/config/bot/black/group")
public class GroupBlackController extends BaseController {

    String prefix = "config/bot/black/group";

    @Resource
    BlackService bs;

    @GetMapping
    public String group() {
        return prefix + "/group";
    }


    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(GroupBlack gb) {
        Page<GroupBlack> list = bs.list(gb);
        return getDataTable(list.getContent(), list.getTotalElements());
    }


    @GetMapping("/add")
    public String add(ModelMap map) {
        SpringUtils.getBean(BotContainer.class).robots.forEach((aLong, bot) -> {
            map.put("group", bot.getGroupList().getData());
        });
        return prefix + "/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(GroupBlack gb) {
        return toAjax(bs.save(gb));
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap map) {
        map.put("black", bs.findByGroupId(id));
        return prefix + "/edit";
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult edit(GroupBlack gb) {
        return toAjax(bs.save(gb));
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("id") Long id) {
        GroupBlack gb = new GroupBlack();
        gb.setId(id);
        return toAjax(bs.remove(gb));
    }

    @PostMapping("/handoff")
    @ResponseBody
    public AjaxResult handoff() {
        return SpringUtils.getBean(HandOff.class).handoff();
    }

}
