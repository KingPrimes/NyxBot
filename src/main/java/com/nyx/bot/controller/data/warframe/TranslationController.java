package com.nyx.bot.controller.data.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.warframe.TranslationRepository;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/translation")
public class TranslationController extends BaseController {

    @Resource
    TranslationRepository repository;

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = success();
        repository.findById(id).ifPresent(t -> ar.put("translation", t));
        return ar;
    }

    @PostMapping("/save")
    public AjaxResult save(@RequestBody Translation t) {
        if (t == null) return error();
        if (t.getEn().trim().isEmpty()) return error("英文不可为空");
        if (t.getCn().trim().isEmpty()) return error("中文不可为空");
        repository.save(t);
        return success();
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody Translation t) {
        return getDataTable(repository.findAllPageable(
                t.getCn(),
                t.getIsPrime(),
                t.getIsSet(),
                PageRequest.of(t.getCurrent() - 1, t.getSize())
        ));
    }

    /**
     * 更新词典
     */
    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
            if (flag) {
                CompletableFuture.runAsync(WarframeDataSource::initTranslation);
            }
        });
        return success("已执行任务！");
    }

    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String, String> commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<Translation> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/translation.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit.get("commit"), branchName, "warframe/translation.json");
            return toAjax(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
