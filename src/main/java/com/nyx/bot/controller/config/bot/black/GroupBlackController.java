package com.nyx.bot.controller.config.bot.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.black.GroupBlack;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/black/group")
public class GroupBlackController extends BaseController {

    @Resource
    BlackService bs;


    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody GroupBlack gb) {
        return getDataTable(bs.list(gb));
    }


    @GetMapping("/add")
    public AjaxResult add() {
        AjaxResult ar = AjaxResult.success();
        SpringUtils.getBean(BotContainer.class).robots.forEach((aLong, bot) -> ar.put(String.valueOf(aLong), bot.getGroupList().getData()));
        return ar;
    }

    @PostMapping("/save")
    public AjaxResult add(@RequestBody GroupBlack gb) {
        if (gb == null) return AjaxResult.error("请先链接机器人");
        return toAjax(bs.save(gb));
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable("id") Long id) {
        return AjaxResult.success().put("black", bs.findByGroupId(id));
    }

    @PostMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.remove(id));
    }

}
