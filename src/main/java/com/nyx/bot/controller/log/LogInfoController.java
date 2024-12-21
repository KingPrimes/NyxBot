package com.nyx.bot.controller.log;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.sys.LogInfo;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.repo.sys.LogInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    @Resource
    LogInfoRepository repository;


    @GetMapping("/info")
    public AjaxResult info(Model model) {
        return success().put("codes", Codes.values());
    }

    // 分页条件查询
    @PostMapping("/info/list")
    public ResponseEntity<?> list(LogInfo info) {
        return getDataTable(repository.findAllPageable(
                info.getCodes() == null ? null : info.getCodes(),
                info.getGroupUid(),
                PageRequest.of(info.getPageNum() - 1, info.getPageSize())));
    }

    @GetMapping("/info/detail/{logId}")
    public AjaxResult detail(@PathVariable("logId") Long logId) {
        AjaxResult ar = success();
        repository.findById(logId).ifPresent(l -> ar.put("info", l));
        return ar;
    }
}
