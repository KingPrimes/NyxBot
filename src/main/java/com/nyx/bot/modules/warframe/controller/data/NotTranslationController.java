package com.nyx.bot.modules.warframe.controller.data;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.Views;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.modules.warframe.entity.NotTranslation;
import com.nyx.bot.modules.warframe.entity.Translation;
import com.nyx.bot.modules.warframe.repo.NotTranslationRepository;
import com.nyx.bot.modules.warframe.service.TranslationService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data/warframe/notTranslation")
public class NotTranslationController extends BaseController {

    @Resource
    NotTranslationRepository notTranslationRepository;
    @Resource
    TranslationService translationService;


    @GetMapping("/add/{id}")
    public AjaxResult add(@PathVariable Long id) {
        AjaxResult ar = success();
        ar.put("id", id);
        notTranslationRepository.findById(id).ifPresent(n -> ar.put("key", n.getNotTranslation()));
        return ar;
    }

    /**
     * 分页查询
     *
     * @param t 查询条件
     */
    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody NotTranslation t) {
        ExampleMatcher notTranslation = ExampleMatcher.matching().withMatcher("notTranslation", ExampleMatcher.GenericPropertyMatcher::contains)
                .withIgnoreCase();
        Example<NotTranslation> notTranslationExample = Example.of(t, notTranslation);

        return getDataTable(notTranslationRepository.findAll(notTranslationExample, PageRequest.of(t.getCurrent() - 1, t.getSize())));
    }

    /**
     * 添加词典
     *
     * @param t 词典内容
     */
    @PostMapping("/save")
    public AjaxResult save(@Validated @RequestBody Translation t) {
        notTranslationRepository.deleteById(t.getId());
        Translation translation = new Translation();
        translation.setCn(t.getCn());
        translation.setEn(t.getEn());
        translation.setIsSet(t.getIsSet());
        translation.setIsPrime(t.getIsPrime());
        return toAjax(translationService.save(translation) != null);
    }


}
