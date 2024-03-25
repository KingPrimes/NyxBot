package com.nyx.bot.controller.log;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.LogInfo;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.repo.sys.LogInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    @Resource
    LogInfoRepository repository;

    String prefix = "log/info";

    @GetMapping("/info")
    public String info(ModelMap map) {
        map.put("code", Codes.values());
        return prefix + "/info";
    }

    // 分页条件查询
    @PostMapping("/info/list")
    @ResponseBody
    public ResponseEntity list(LogInfo info) {
        return getDataTable(repository.findAllPageable(
                info.getCodes(),
                info.getGroupUid(),
                PageRequest.of(info.getPageNum() - 1, info.getPageSize())));
    }

    @GetMapping("/info/detail/{logId}")
    public String detail(@PathVariable("logId") Long logId, ModelMap map) {
        map.put("info", repository.findById(logId).get());
        return prefix + "/detail";
    }
}
