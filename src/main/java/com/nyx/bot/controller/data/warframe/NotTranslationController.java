package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.NotTranslation;
import com.nyx.bot.entity.warframe.Translation;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.repo.warframe.NotTranslationRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> list(NotTranslation t) {
        ExampleMatcher notTranslation = ExampleMatcher.matching().withMatcher("notTranslation", ExampleMatcher.GenericPropertyMatcher::contains)
                .withIgnoreCase();
        Example<NotTranslation> notTranslationExample = Example.of(t, notTranslation);

        return getDataTable(notTranslationRepository.findAll(notTranslationExample, PageRequest.of(t.getPageNum() - 1, t.getPageSize())));
    }

    /**
     * 添加词典
     *
     * @param t 词典内容
     */
    @PostMapping("/save")
    public AjaxResult save(Translation t) {
        notTranslationRepository.deleteById(t.getId());
        Translation translation = new Translation();
        translation.setCn(t.getCn());
        translation.setEn(t.getEn());
        translation.setIsSet(t.getIsSet());
        translation.setIsPrime(t.getIsPrime());
        return toAjax(translationService.save(translation) != null);
    }


}
