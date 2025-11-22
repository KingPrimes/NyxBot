package com.nyx.bot.controller.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.dao.LogInfoWebSocketDto;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.event.LogEvent;
import com.nyx.bot.service.log.LogInfoMapper;
import com.nyx.bot.service.log.LogSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日志导出控制器
 * 提供日志导出和统计接口
 *
 * @author KinrPrimes
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@Tag(name = "日志管理", description = "日志查询、导出和统计接口")
public class LogExportController extends BaseController {

    @Resource
    private LogSearchService logSearchService;

    @Resource
    private LogCacheManager logCacheManager;

    @Resource
    private LogInfoMapper logInfoMapper;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 导出日志为 TXT 文件
     *
     * @param keyword   关键词（可选）
     * @param startTime 开始时间戳（可选）
     * @param endTime   结束时间戳（可选）
     * @param levels    日志级别列表（可选）
     * @param response  HTTP 响应
     * @throws IOException IO 异常
     */
    @GetMapping("/export/txt")
    @Operation(summary = "导出日志为TXT文件", description = "支持按关键词、时间范围、级别过滤后导出")
    public void exportToTxt(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间戳(毫秒)") @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间戳(毫秒)") @RequestParam(required = false) Long endTime,
            @Parameter(description = "日志级别列表") @RequestParam(required = false) List<String> levels,
            HttpServletResponse response
    ) throws IOException {
        log.debug("导出日志为TXT: keyword={}, startTime={}, endTime={}, levels={}",
                keyword, startTime, endTime, levels);

        // 搜索日志
        List<LogEvent> logs = logSearchService.searchLogs(
                keyword, startTime, endTime, levels, false
        );

        // 设置响应头
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=logs_" + System.currentTimeMillis() + ".txt");

        // 写入日志
        try (PrintWriter writer = response.getWriter()) {
            writer.println("# 日志导出");
            writer.println("# 导出时间: " + new java.util.Date());
            writer.println("# 总条数: " + logs.size());
            writer.println("# 过滤条件: keyword=" + keyword + ", levels=" + levels);
            writer.println("# " + "=".repeat(80));
            writer.println();

            for (LogEvent logEvent : logs) {
                writer.printf("%s %s [%s] %s : %s%n",
                        logEvent.getLevel(),
                        logEvent.getTime(),
                        logEvent.getThread(),
                        logEvent.getPack(),
                        logEvent.getLog()
                );
            }
        }

        log.info("成功导出 {} 条日志为TXT格式", logs.size());
    }

    /**
     * 导出日志为 JSON 文件
     *
     * @param keyword   关键词（可选）
     * @param startTime 开始时间戳（可选）
     * @param endTime   结束时间戳（可选）
     * @param levels    日志级别列表（可选）
     * @param response  HTTP 响应
     * @throws IOException IO 异常
     */
    @GetMapping("/export/json")
    @Operation(summary = "导出日志为JSON文件", description = "支持按关键词、时间范围、级别过滤后导出")
    public void exportToJson(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间戳(毫秒)") @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间戳(毫秒)") @RequestParam(required = false) Long endTime,
            @Parameter(description = "日志级别列表") @RequestParam(required = false) List<String> levels,
            HttpServletResponse response
    ) throws IOException {
        log.info("导出日志为JSON: keyword={}, startTime={}, endTime={}, levels={}",
                keyword, startTime, endTime, levels);

        // 搜索日志
        List<LogEvent> logs = logSearchService.searchLogs(
                keyword, startTime, endTime, levels, false
        );

        // 转换为 DTO
        List<LogInfoWebSocketDto> dtoList = logs.stream()
                .map(logInfoMapper::toDto)
                .collect(Collectors.toList());

        // 设置响应头
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=logs_" + System.currentTimeMillis() + ".json");

        // 写入 JSON
        Map<String, Object> result = new HashMap<>();
        result.put("exportTime", System.currentTimeMillis());
        result.put("total", dtoList.size());
        result.put("filter", buildFilter(keyword, startTime, endTime, levels, false));
        result.put("logs", dtoList);

        response.getWriter().write(objectMapper.writeValueAsString(result));

        log.info("成功导出 {} 条日志为JSON格式", logs.size());
    }

    /**
     * 获取日志统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取日志统计信息", description = "返回当前缓存的日志数量和各级别分布")
    @SuppressWarnings("ALL")
    public AjaxResult getLogStats() {
        List<LogEvent> allLogs = logCacheManager.getAllLogs();

        // 统计各级别数量
        Map<String, Long> levelCounts = allLogs.stream()
                .collect(Collectors.groupingBy(
                        LogEvent::getLevel,
                        Collectors.counting()
                ));

        // 构建响应数据
        return success()
                .put("total", allLogs.size())
                .put("maxCacheSize", logCacheManager.getMaxCacheSize())
                .put("levelCounts", levelCounts)
                .put("cacheStatus", logCacheManager.getStatus())
                .put("statistics", logSearchService.getStatistics());
    }


    /**
     * 搜索日志
     *
     * @param keyword   关键词（可选）
     * @param startTime 开始时间戳（可选）
     * @param endTime   结束时间戳（可选）
     * @param levels    日志级别列表（可选）
     * @param useRegex  是否使用正则表达式（可选）
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索日志", description = "支持多条件组合搜索")
    @SuppressWarnings("ALL")
    public AjaxResult searchLogs(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间戳(毫秒)") @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间戳(毫秒)") @RequestParam(required = false) Long endTime,
            @Parameter(description = "日志级别列表") @RequestParam(required = false) List<String> levels,
            @Parameter(description = "是否使用正则表达式") @RequestParam(required = false, defaultValue = "false") boolean useRegex
    ) {
        log.info("搜索日志: keyword={}, startTime={}, endTime={}, levels={}, useRegex={}",
                keyword, startTime, endTime, levels, useRegex);

        // 搜索日志
        List<LogEvent> logs = logSearchService.searchLogs(
                keyword, startTime, endTime, levels, useRegex
        );

        // 转换为 DTO
        List<LogInfoWebSocketDto> dtoList = logs.stream()
                .map(logInfoMapper::toDto)
                .collect(Collectors.toList());

        // 构建响应
        return success()
                .put("total", dtoList.size())
                .put("filter", buildFilter(keyword, startTime, endTime, levels, useRegex))
                .put("logs", dtoList);
    }

    private Map<String, Object> buildFilter(
            String keyword,
            Long startTime,
            Long endTime,
            List<String> levels,
            boolean useRegex
    ) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("keyword", keyword != null ? keyword : "");
        filter.put("startTime", startTime != null ? startTime : "");
        filter.put("endTime", endTime != null ? endTime : "");
        filter.put("levels", levels != null ? levels : List.of());
        filter.put("useRegex", useRegex);
        return filter;
    }
}