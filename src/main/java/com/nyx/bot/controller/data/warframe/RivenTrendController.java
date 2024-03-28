package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.RivenTrend;
import com.nyx.bot.enums.AsyncBeanName;
import com.nyx.bot.enums.RivenTrendTypeEnum;
import com.nyx.bot.plugin.warframe.utils.RivenDispositionUpdates;
import com.nyx.bot.repo.warframe.RivenTrendRepository;
import com.nyx.bot.utils.AsyncUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/data/warframe/rivenTrend")
public class RivenTrendController extends BaseController {
    String prefix = "data/warframe/rivenTrend/";

    @Resource
    RivenTrendRepository repository;


    @GetMapping
    public String index() {
        return prefix + "rivenTrend";
    }

    @GetMapping("/add")
    public String add(ModelMap map) {
        map.put("types", Arrays.stream(RivenTrendTypeEnum.values()).toList());
        return prefix + "add";
    }

    @GetMapping("/push")
    public String push() {
        return prefix + "push";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, ModelMap map) {
        map.put("types", RivenTrendTypeEnum.values());
        map.put("translation", repository.findById(id));
        return prefix + "edit";
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(RivenTrend t) {
        return toAjax(repository.saveAndFlush(t) != null);
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity list(RivenTrend rt) {
        return getDataTable(repository.findAllPageable(rt.getTrendName(),
                PageRequest.of(
                        rt.getPageNum() - 1,
                        rt.getPageSize()
                )
        ));
    }


    @PostMapping("/init")
    @ResponseBody
    public AjaxResult init() {
        WarframeDataSource.cloneDataSource();
        WarframeDataSource.getRivenTrend();
        return success("已执行任务！");
    }

    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        AsyncUtils.me().execute(() -> {
            new RivenDispositionUpdates().upRivenTrend();
        }, AsyncBeanName.InitData);
        return success("已执行任务！");
    }

    @PostMapping("/push")
    @ResponseBody
    public AjaxResult push(String commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<RivenTrend> all = repository.findAll();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/riven_trend.json", jsonString);
            build.add().commit(commit).push();
            return toAjax(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
