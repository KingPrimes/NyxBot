package com.nyx.bot.modules.bot.controller.white;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.bot.entity.white.GroupWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
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
