package com.nyx.bot.controller.data.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.RivenTrendEnum;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.repo.warframe.RivenTrendRepository;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/data/warframe/rivenTrend")
public class RivenTrendController extends BaseController {

    @Resource
    RivenTrendRepository repository;

    @GetMapping("/add")
    public AjaxResult add() {
        return success().put("types", RivenTrendTypeEnum.values());
    }

    @GetMapping("/edit/{id}")
    public AjaxResult edit(@PathVariable Long id) {
        AjaxResult ar = success().put("types", RivenTrendTypeEnum.values());
        if (ar != null) {
            ar.put("translation", repository.findById(id));
        }
        return ar;
    }

    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody RivenTrend t) {
        t.setOldDot(RivenTrendEnum.getRivenTrendDot(t.getOldNum()));
        t.setNewDot(RivenTrendEnum.getRivenTrendDot(t.getNewNum()));
        return toAjax(Math.toIntExact(repository.save(t).getId()));
    }

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody RivenTrend rt) {
        return getDataTable(repository.findAllPageable(rt.getTrendName(),
                PageRequest.of(
                        rt.getCurrent() - 1,
                        rt.getSize()
                )
        ));
    }


    @PostMapping("/init")
    public AjaxResult init() {
        CompletableFuture.supplyAsync(WarframeDataSource::cloneDataSource).thenAccept(flag -> {
            if (flag) {
                CompletableFuture.runAsync(WarframeDataSource::getRivenTrend);
            }
        });
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/update")
    public AjaxResult update() {
        AsyncUtils.me().execute(() -> new RivenDispositionUpdates().upRivenTrend(), AsyncBeanName.InitData);
        return success(I18nUtils.RequestTaskRun());
    }

    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String, String> commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            build.pull();
            List<RivenTrend> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/riven_trend.json", jsonString);
            String branchName = DateUtils.getDate(new Date(), DateUtils.NOT_HMS);
            build.pushBranchCheckout(commit.get("commit"), branchName, "warframe/riven_trend.json");
            return success();
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
