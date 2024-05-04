package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.warframe.TranslationRepository;
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
@RequestMapping("/data/warframe/translation")
public class TranslationController extends BaseController {
    String prefix = "data/warframe/translation/";

    @Resource
    TranslationRepository repository;


    @GetMapping
    public String html() {
        return prefix + "translation";
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "add";
    }

    @GetMapping("/push")
    public String push() {
        return prefix + "push";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        repository.findById(id).ifPresent(t -> model.addAttribute("translation", t));
        return prefix + "edit";
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(Translation t) {
        repository.save(t);
        return success();
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(Translation t) {
        return getDataTable(repository.findAllPageable(
                t.getCn(),
                t.getIsPrime(),
                t.getIsSet(),
                PageRequest.of(t.getPageNum() - 1, t.getPageSize())
        ));
    }

    /**
     * 更新词典
     */
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.cloneDataSource();
        WarframeDataSource.initTranslation();
        return success("已执行任务！");
    }

    @PostMapping("/push")
    @ResponseBody
    public AjaxResult push(String commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<Translation> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/translation.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit, branchName, "warframe/translation.json");
            return toAjax(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
