package com.nyx.bot.controller.log;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.system.entity.LogInfo;
import com.nyx.bot.modules.system.repo.LogInfoRepository;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    @Resource
    LogInfoRepository repository;


    @GetMapping("/codes")
    public AjaxResult info() {
        return success().put("data", Arrays.stream(Codes.values())
                .map(c -> Map.of("label", StringUtils.removeMatcher(c.getComm()), "value", c.name()))
                .collect(Collectors.toList())
        );
    }

    // 分页条件查询
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody LogInfo info) {
        return getDataTable(repository.findAllPageable(
                info.getCodes(),
                info.getGroupUid(),
                PageRequest.of(info.getCurrent() - 1, info.getSize())));
    }

    @GetMapping("/detail/{logId}")
    public AjaxResult detail(@PathVariable("logId") Long logId) {
        AjaxResult ar = success();
        repository.findById(logId).ifPresent(l -> ar.put("data", l));
        return ar;
    }
}
