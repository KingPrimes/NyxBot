package com.nyx.bot.controller.config.bot.white;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.controller.config.bot.HandOff;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.white.GroupWhite;
import com.nyx.bot.repo.impl.white.WhiteService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/config/bot/white/group")
public class GroupWhiteController extends BaseController {

    String prefix = "config/bot/white/group";

    @Resource
    WhiteService whiteService;

    @GetMapping
    public String group() {
        return prefix + "/group";
    }


    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(GroupWhite white) {
        Page<GroupWhite> list = whiteService.list(white);
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
    public AjaxResult add(GroupWhite white) {
        whiteService.save(white);
        return success();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap map) {
        map.put("white", whiteService.findByGroup(id));
        return prefix + "/edit";
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult edit(GroupWhite white) {
        whiteService.save(white);
        return success();
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("id") Long id) {
        whiteService.remove(id);
        return success();
    }

    @PostMapping("/handoff")
    @ResponseBody
    public AjaxResult handoff() {
        return SpringUtils.getBean(HandOff.class).handoff();
    }

}
