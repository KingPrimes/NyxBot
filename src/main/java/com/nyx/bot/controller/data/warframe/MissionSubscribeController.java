package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/data/warframe/subscribe")
public class MissionSubscribeController extends BaseController {
    String prefix = "data/warframe/subscribe/";

    @Resource
    MissionSubscribeRepository repository;


    @GetMapping
    public String subscribe(Model model) {
        model.addAttribute("sub", SubscribeEnums.values());
        return prefix + "subscribe";
    }

    @GetMapping("/detail/{subGroup}")
    public String detail(@PathVariable Long subGroup, Model model) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        model.addAttribute("group", group);
        return prefix + "detail";
    }

    @GetMapping("/edit/{subGroup}")
    public String edit(@PathVariable Long subGroup, Model model) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        model.addAttribute("group", group);
        return prefix + "edit";
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
                ).map(subscribe -> {
                    subscribe.setSubUsers(
                            subscribe.getSubUsers().stream()
                                    .peek(s ->
                                            s.setTypeList(
                                                    s.getTypeList()
                                                            .stream()
                                                            .peek(t -> t.setSubscribeType(t.getSubscribe().getNAME()))
                                                            .toList()
                                            )
                                    )
                                    .toList()
                    );
                    return subscribe;
                })
        );
    }


}
