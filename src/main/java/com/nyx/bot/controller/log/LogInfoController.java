package com.nyx.bot.controller.log;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.sys.LogInfo;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.repo.impl.sys.LogInfoService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    @Resource
    LogInfoService repository;

    String prefix = "log/info";

    @GetMapping("/info")
    public String info(ModelMap map) {
        map.put("code", Codes.values());
        return prefix + "/info";
    }

    // 分页条件查询
    @PostMapping("/info/list")
    @ResponseBody
    public TableDataInfo list(LogInfo info) {
        Page<LogInfo> list = repository.list(info);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    @GetMapping("/info/detail/{logId}")
    public String detail(@PathVariable("logId") Long logId, ModelMap map) {
        map.put("loginfo", repository.findById(logId).get());
        return prefix + "/detail";
    }
}
