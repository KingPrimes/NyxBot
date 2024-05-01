package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.MissionSubscribeRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/subscribe")
public class MissionSubscribeController extends BaseController {
    String prefix = "data/warframe/";

    @Resource
    MissionSubscribeRepository repository;


    @GetMapping
    public String subscribe(Model model) {
        model.addAttribute("sub", SubscribeEnums.values());
        return prefix + "subscribe";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(MissionSubscribe ms) {

        return getDataTable(
                repository.findAllPageable(
                        ms.getSubGroup(),
                        PageRequest.of(
                                ms.getPageNum() - 1,
                                ms.getPageSize()
                        )
                )
        );
    }

}
