package com.nyx.bot.modules.bot.controller.black;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.bot.entity.black.GroupBlack;
import com.nyx.bot.modules.bot.service.black.BlackService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
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

    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody GroupBlack gb) {
        return toAjax(bs.save(gb));
    }

    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        return toAjax(bs.remove(id));
    }

}
