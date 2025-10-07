package com.nyx.bot.modules.warframe.controller.template;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.HtmlToImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/template")
@Slf4j
public class TemplateController {

    String path = HtmlToImage.HTML_PATH + "/html";

    @GetMapping
    public AjaxResult getTemplate(@RequestParam String fileName) {
        log.debug("获取模板: {}", fileName);
        return AjaxResult.success().data(FileUtils.readFileToString(path + "/" + fileName));
    }

    @GetMapping("/list")
    public AjaxResult getTemplateList() {
        List<String> strings = FileUtils.getFilesName(path).orElse(List.of());
        log.debug("模板列表: {}", strings);
        return AjaxResult.success().data(strings);
    }

    @PostMapping("/save")
    public AjaxResult saveTemplate(@RequestBody Map<String, String> payload) {
        String fileName = payload.get("fileName");
        String html = payload.get("html");
        log.info("保存模板: {}，内容: {}", fileName, html);
        return AjaxResult.success();
    }

}
