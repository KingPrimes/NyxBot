package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.NotTranslation;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.NotTranslationRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/data/warframe/notTranslation")
public class NotTranslationController extends BaseController {
    String prefix = "data/warframe/notTranslation/";

    @Resource
    NotTranslationRepository notTranslationRepository;
    @Resource
    TranslationService translationService;

    /**
     * HTML页面路径
     */
    @GetMapping
    public String html() {
        return prefix + "notTranslation";
    }

    @GetMapping("/add/{id}")
    public String add(@PathVariable Long id, ModelMap map) {
        map.put("id", id);
        notTranslationRepository.findById(id).ifPresent(n -> map.put("key", n.getNotTranslation()));
        return prefix + "add";
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity list(NotTranslation t) {
        return getDataTable(notTranslationRepository.findAll(
                PageRequest.of(t.getPageNum() - 1, t.getPageSize())
        ));
    }

    /**
     * 添加词典
     *
     * @param t 词典内容
     */
    @PostMapping("/save")
    @ResponseBody
    public AjaxResult save(Translation t) {
        notTranslationRepository.deleteById(t.getId());
        return toAjax(translationService.save(t) != null);
    }


}
