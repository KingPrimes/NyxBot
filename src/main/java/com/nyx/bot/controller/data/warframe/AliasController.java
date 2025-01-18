package com.nyx.bot.controller.data.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Alias;
import com.nyx.bot.repo.warframe.AliasRepository;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/data/warframe/alias")
public class AliasController extends BaseController {

    @Resource
    AliasRepository repository;

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody Alias alias) {
        return getDataTable(repository.findByLikeCn(alias.getCn(), PageRequest.of(alias.getCurrent() - 1, alias.getSize())));
    }

    @PostMapping("/update")
    public AjaxResult update() {
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
            if (flag) {
                CompletableFuture.runAsync(WarframeDataSource::getAlias);
            }
        });
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody Alias a) {
        if (!a.isValidEnglish()) {
            return error(I18nUtils.message("request.valid.alias.en"));
        }
        if (!a.isValidChinese()) {
            return error(I18nUtils.message("request.valid.alias.ch"));
        }
        Optional<Alias> alias = repository.findByCnAndEn(a.getCn(), a.getEn());
        if (alias.isPresent()) {
            return error(I18nUtils.message("request.error.data.already.exists"));
        }
        repository.save(a);
        return success();
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = success();
        repository.findById(id).ifPresent(a -> ar.put("alias", a));
        return ar;
    }

    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String, String> commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<Alias> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/alias.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit.get("commit"), branchName, "warframe/alias.json");
            return toAjax(true);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
