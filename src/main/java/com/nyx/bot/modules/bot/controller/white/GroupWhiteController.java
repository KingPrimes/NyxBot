package com.nyx.bot.modules.bot.controller.white;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.bot.entity.white.GroupWhite;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 群聊白名单
 */
@RestController
@RequestMapping("/config/bot/white/group")
public class GroupWhiteController extends BaseController {

    private final WhiteService ws;

    private final BlackService bs;

    public GroupWhiteController(WhiteService ws, BlackService bs) {
        this.ws = ws;
        this.bs = bs;
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody GroupWhite white) {
        return getDataTable(ws.list(white));
    }


    @PostMapping("/save")
    public ApiResponse<Void> add(@Validated @RequestBody GroupWhite white) {
        if (bs.isBlack(white.getGroupUid(), null)) {
            return toAjax(ws.save(white) != null);
        }
        return error(HttpStatus.BAD_REQUEST, I18nUtils.BWBlackExist());
    }

    @DeleteMapping("/remove/{id}")
    public ApiResponse<Void> remove(@PathVariable("id") Long id) {
        ws.remove(id);
        return success();
    }

}
