package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.repo.warframe.AliasRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/alias")
public class AliasController extends BaseController {

    String prefix = "data/warframe/";
    @Resource
    AliasRepository repository;


    @GetMapping
    public String alias() {
        return prefix + "alias";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(Alias alias) {
        return getDataTable(repository.findAll(PageRequest.of(alias.getPageNum() - 1, alias.getPageSize())));
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.cloneDataSource();
        WarframeDataSource.getAlias();
        return success("已执行任务！");
    }

}
