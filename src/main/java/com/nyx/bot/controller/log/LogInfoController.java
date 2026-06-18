package com.nyx.bot.controller.log;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.LogTitleEnum;
import com.nyx.bot.modules.system.entity.LogInfo;
import com.nyx.bot.modules.system.repo.LogInfoRepository;
import com.nyx.bot.utils.StringUtils;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/log")
public class LogInfoController extends BaseController {

    private final LogInfoRepository repository;

    public LogInfoController(LogInfoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/codes")
    public ApiResponse<Object> info() {
        return success(Arrays.stream(Codes.values())
                .map(c -> Map.of("label", StringUtils.removeMatcher(c.getComm()), "value", StringUtils.removeMatcher(c.getComm())))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/titles")
    public ApiResponse<Object> logTitle() {
        return success(Arrays.stream(LogTitleEnum.values())
                .map(t -> Map.of("label", t.getTitle(), "value", t.name()))
                .toList());
    }

    // 分页条件查询
    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody LogInfo info) {
        return getDataTable(repository.findAllPageable(
                info.getTitle(),
                info.getCode(),
                info.getGroupUid(),
                PageRequest.of(info.getCurrent() - 1, info.getSize())));
    }

    @GetMapping("/detail/{logId}")
    public ApiResponse<?> detail(@NonNull @PathVariable("logId") Long logId) {
        var logOpt = repository.findById(logId);
        if (logOpt.isPresent()) {
            return success(logOpt.get());
        }
        return success();
    }
}
