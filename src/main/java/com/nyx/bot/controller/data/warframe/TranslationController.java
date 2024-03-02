package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.gitutils.JgitUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/data/warframe/translation")
public class TranslationController extends BaseController {
    String prefix = "data/warframe/translation/";

    @Resource
    TranslationService tlService;


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
    public String edit(@PathVariable Long id, ModelMap map) {
        map.put("translation", tlService.findById(id));
        return prefix + "edit";
    }

    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(Translation t) {
        return toAjax(tlService.save(t) != null);
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Translation t) {
        Page<Translation> list = tlService.list(t);
        return getDataTable(list.getContent(), list.getTotalElements());
    }

    /**
     * 更新词典
     */
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult update() {
        WarframeDataSource.initTranslation(ApiUrl.WARFRAME_DATA_SOURCE_GIT_HUB);
        return success("已执行任务！");
    }

    @PostMapping("/push")
    @ResponseBody
    public AjaxResult push(String commit) {
        try {
            JgitUtil build = JgitUtil.Build();
            List<Translation> all = tlService.findAllToList();
            String jsonString = pushJson(all);
            FileUtils.writeFile(JgitUtil.lockPath + "/warframe/translation.json", jsonString);
            build.add().commit(commit).push();
            return toAjax(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
