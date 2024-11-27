package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.repo.warframe.AliasRepository;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/data/warframe/alias")
public class AliasController extends BaseController {

    String prefix = "data/warframe/alias/";
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

    @GetMapping("/add")
    public String add() {
        return prefix + "add";
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(Alias a) {
        if (a == null) {
            return error("参数错误！");
        }
        if (a.getCn() == null) {
            return error("中文不能为空！");
        }
        if (a.getEn() == null) {
            return error("英文不能为空！");
        }
        if (a.getCn().trim().isEmpty()) {
            return error("中文不能为空！");
        }
        if (a.getEn().trim().isEmpty()) {
            return error("英文不能为空！");
        }
        Alias byCnAndEn = repository.findByCnAndEn(a.getCn(), a.getEn());
        if (byCnAndEn != null) {
            return error("该别名已存在！");
        }
        repository.save(a);
        return success();
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        repository.findById(id).ifPresent(a -> model.addAttribute("alias", a));
        return prefix + "edit";
    }

    @PostMapping("/push")
    @ResponseBody
    public AjaxResult push(String commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<Alias> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/alias.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit, branchName, "warframe/alias.json");
            return toAjax(true);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
