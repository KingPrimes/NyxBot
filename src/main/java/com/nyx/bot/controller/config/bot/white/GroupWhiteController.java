package com.nyx.bot.controller.config.bot.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.white.GroupWhite;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.repo.impl.black.BlackService;
import com.nyx.bot.repo.impl.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config/bot/white/group")
public class GroupWhiteController extends BaseController {

    @Resource
    WhiteService ws;

    @Resource
    BlackService bs;


    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody GroupWhite white) {
        return getDataTable(ws.list(white));
    }


    @PostMapping("/save")
    public AjaxResult add(@Validated @RequestBody GroupWhite white) {
        if (bs.isBlack(white.getGroupUid(), null)) {
            return toAjax(ws.save(white) != null);
        }
        return error(HttpCodeEnum.FAIL, I18nUtils.BWBlackExist());
    }


    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id) {
        ws.remove(id);
        return success();
    }

}
